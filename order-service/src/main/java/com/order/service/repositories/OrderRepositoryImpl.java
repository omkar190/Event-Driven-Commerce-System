package com.order.service.repositories;

import com.order.service.config.RabbitMQConfig;
import com.order.service.entities.Order;
import com.order.service.mapper.OrderRowMapper;
import com.order.service.queries.OrderQueries;
import com.order.service.queries.ProductQueries;
import com.order.service.queries.OutboxQueries;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements CustomOrderRepository {

    private final DatabaseClient databaseClient;
    private final OrderRowMapper mapper = new OrderRowMapper();
    private final TransactionalOperator txOperator;
    private final RabbitTemplate rabbitTemplate;

    public OrderRepositoryImpl(DatabaseClient databaseClient,
                               TransactionalOperator txOperator,
                               RabbitTemplate rabbitTemplate) {
        this.databaseClient = databaseClient;
        this.txOperator = txOperator;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Mono<Order> insertOrder(Order order) {

        UUID eventId = UUID.randomUUID();

        String payload = """
            {
              "orderId": "%s",
              "userId": "%s",
              "productId": "%s",
              "quantity": %d,
              "amount": %s,
              "address": "%s",
              "mobileNumber": "%s",
              "stripeClientSecret": "%s"
            }
            """.formatted(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getQuantity(),
                order.getAmount(),
                order.getAddress(),
                order.getMobileNumber(),
                order.getStripeClientSecret()
        );

        Mono<Void> txFlow = databaseClient.sql(OrderQueries.INSERT)
                .bind("id", order.getId())
                .bind("userId", order.getUserId())
                .bind("productId", order.getProductId())
                .bind("quantity", order.getQuantity())
                .bind("amount", order.getAmount())
                .bind("address", order.getAddress())
                .bind("status", order.getStatus())
                .bind("createdAt", order.getCreatedAt())
                .bind("mobileNumber", order.getMobileNumber())
                .bind("stripeClientSecret", order.getStripeClientSecret())
                .then()
                .then(
                        databaseClient.sql(OutboxQueries.INSERT)
                                .bind("id", eventId)
                                .bind("aggregateType", "ORDER")
                                .bind("aggregateId", order.getId())
                                .bind("eventType", "ORDER_CREATED")
                                .bind("payload", payload)
                                .bind("status", "CREATED")
                                .bind("createdAt", LocalDateTime.now())
                                .then()
                );

        return txOperator.transactional(txFlow)
                .thenReturn(order)
                .flatMap(savedOrder ->
                        Mono.fromCallable(() -> {
                                    rabbitTemplate.convertAndSend(
                                            RabbitMQConfig.ORDER_EXCHANGE,
                                            RabbitMQConfig.ORDER_ROUTING_KEY,
                                            payload
                                    );
                                    return savedOrder;
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(sent ->
                                        databaseClient.sql(OutboxQueries.UPDATE_STATUS)
                                                .bind("id", eventId)
                                                .then()
                                                .then(
                                                        databaseClient.sql(OrderQueries.UPDATE_STATUS)
                                                                .bind("id", savedOrder.getId())
                                                                .then()
                                                )
                                                .thenReturn(savedOrder)
                                )
                                .onErrorResume(ex -> {
                                    System.err.println("RabbitMQ failed -> " + ex.getMessage());
                                    return Mono.just(savedOrder);
                                })
                );
    }

    @Override
    public Mono<Order> findById(String orderId) {
        return databaseClient.sql(OrderQueries.FIND_BY_ID)
                .bind("id", orderId)
                .map(mapper::apply)
                .one();
    }

    @Override
    public Mono<Order> confirmPayment(String orderId, String stripePaymentIntentId, String productName) {

        UUID eventId = UUID.randomUUID();

        return findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order -> {
                    if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"SENT".equals(order.getStatus())) {
                        return Mono.error(new RuntimeException("Order already processed"));
                    }

                    String notificationPayload = """
                        {
                          "email": "%s",
                          "type": "ORDER_SUCCESS",
                          "orderId": "%s",
                          "productName": "%s",
                          "quantity": %d,
                          "amount": "%s",
                          "address": "%s"
                        }
                        """.formatted(
                            order.getUserId(),
                            order.getId(),
                            productName,
                            order.getQuantity(),
                            "$" + order.getAmount().toPlainString(),
                            order.getAddress()
                    );

                    String outboxPayload = """
                        {
                          "orderId": "%s",
                          "userId": "%s",
                          "status": "CONFIRMED",
                          "stripePaymentIntentId": "%s"
                        }
                        """.formatted(order.getId(), order.getUserId(), stripePaymentIntentId);

                    Mono<Void> txFlow = databaseClient.sql(OrderQueries.UPDATE_PAYMENT_SUCCESS)
                            .bind("id", orderId)
                            .bind("stripePaymentIntentId", stripePaymentIntentId)
                            .fetch()
                            .rowsUpdated()
                            .flatMap(rows -> {
                                if (rows == 0) {
                                    return Mono.error(new RuntimeException("Order update failed"));
                                }
                                return Mono.empty();
                            })
                            .then(
                                    databaseClient.sql(ProductQueries.REDUCE_STOCK)
                                            .bind("id", order.getProductId())
                                            .bind("quantity", order.getQuantity())
                                            .fetch()
                                            .rowsUpdated()
                                            .flatMap(rowsUpdated -> {
                                                if (rowsUpdated == 0) {
                                                    return Mono.error(
                                                            new RuntimeException("Insufficient stock or product not found")
                                                    );
                                                }
                                                return Mono.empty();
                                            })
                            )
                            .then(
                                    databaseClient.sql(OutboxQueries.INSERT)
                                            .bind("id", eventId)
                                            .bind("aggregateType", "ORDER")
                                            .bind("aggregateId", order.getId())
                                            .bind("eventType", "ORDER_CONFIRMED")
                                            .bind("payload", outboxPayload)
                                            .bind("status", "CREATED")
                                            .bind("createdAt", LocalDateTime.now())
                                            .then()
                            );

                    order.setStatus("CONFIRMED");
                    order.setStripePaymentIntentId(stripePaymentIntentId);

                    return txOperator.transactional(txFlow)
                            .thenReturn(order)
                            .flatMap(confirmedOrder ->
                                    Mono.fromCallable(() -> {
                                                rabbitTemplate.convertAndSend(
                                                        RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                                        RabbitMQConfig.ORDER_NOTIFICATION_ROUTING_KEY,
                                                        notificationPayload
                                                );
                                                return confirmedOrder;
                                            })
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .flatMap(sent ->
                                                    databaseClient.sql(OutboxQueries.UPDATE_STATUS)
                                                            .bind("id", eventId)
                                                            .then()
                                                            .thenReturn(sent)
                                            )
                                            .onErrorResume(ex -> {
                                                System.err.println("Notification failed -> " + ex.getMessage());
                                                return Mono.just(confirmedOrder);
                                            })
                            );
                });
    }

    @Override
    public Mono<Order> failPayment(String orderId, String productName) {

        UUID eventId = UUID.randomUUID();

        return findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order -> {
                    if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"SENT".equals(order.getStatus())) {
                        return Mono.just(order);
                    }

                    String notificationPayload = """
                        {
                          "email": "%s",
                          "type": "ORDER_FAILED",
                          "orderId": "%s",
                          "productName": "%s",
                          "quantity": %d,
                          "amount": "%s",
                          "address": "%s"
                        }
                        """.formatted(
                            order.getUserId(),
                            order.getId(),
                            productName,
                            order.getProductId(),
                            order.getQuantity(),
                            "$" + order.getAmount().toPlainString(),
                            order.getAddress()
                    );

                    String outboxPayload = """
                        {
                          "orderId": "%s",
                          "userId": "%s",
                          "status": "PAYMENT_FAILED"
                        }
                        """.formatted(order.getId(), order.getUserId());

                    Mono<Void> txFlow = databaseClient.sql(OrderQueries.UPDATE_PAYMENT_FAILED)
                            .bind("id", orderId)
                            .fetch()
                            .rowsUpdated()
                            .then()
                            .then(
                                    databaseClient.sql(OutboxQueries.INSERT)
                                            .bind("id", eventId)
                                            .bind("aggregateType", "ORDER")
                                            .bind("aggregateId", order.getId())
                                            .bind("eventType", "ORDER_PAYMENT_FAILED")
                                            .bind("payload", outboxPayload)
                                            .bind("status", "CREATED")
                                            .bind("createdAt", LocalDateTime.now())
                                            .then()
                            );

                    order.setStatus("PAYMENT_FAILED");

                    return txOperator.transactional(txFlow)
                            .thenReturn(order)
                            .flatMap(failedOrder ->
                                    Mono.fromCallable(() -> {
                                                rabbitTemplate.convertAndSend(
                                                        RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                                        RabbitMQConfig.ORDER_NOTIFICATION_ROUTING_KEY,
                                                        notificationPayload
                                                );
                                                return failedOrder;
                                            })
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .flatMap(sent ->
                                                    databaseClient.sql(OutboxQueries.UPDATE_STATUS)
                                                            .bind("id", eventId)
                                                            .then()
                                                            .thenReturn(sent)
                                            )
                                            .onErrorResume(ex -> {
                                                System.err.println("Notification failed -> " + ex.getMessage());
                                                return Mono.just(failedOrder);
                                            })
                            );
                });
    }
}