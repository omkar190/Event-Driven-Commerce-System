package com.order.service.repositories;

import com.order.service.config.RabbitMQConfig;
import com.order.service.entities.Order;
import com.order.service.mapper.OrderRowMapper;
import com.order.service.queries.OrderQueries;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import com.order.service.queries.OutboxQueries;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements CustomOrderRepository {

    private final DatabaseClient databaseClient;
    private final OrderRowMapper mapper = new OrderRowMapper();
    private final TransactionalOperator txOperator;
    private final RabbitTemplate rabbitTemplate;

    public OrderRepositoryImpl(DatabaseClient databaseClient, TransactionalOperator txOperator, RabbitTemplate rabbitTemplate) {
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
              "amount": %s
            }
            """.formatted(order.getId(), order.getUserId(), order.getAmount());

        Mono<Void> txFlow = databaseClient.sql(OrderQueries.INSERT)
                .bind("id", order.getId())
                .bind("userId", order.getUserId())
                .bind("status", order.getStatus())
                .bind("amount", order.getAmount())
                .bind("createdAt", order.getCreatedAt())
                .then()

                .then(
                        databaseClient.sql(OutboxQueries.INSERT)
                                .bind("id", eventId)
                                .bind("aggregateType", "ORDER")
                                .bind("aggregateId", order.getId())
                                .bind("eventType", "ORDER_CREATED")
                                .bind("payload", payload)
                                .bind("status", "PENDING")
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

    //Test Method to check transaction behaviour
    public Mono<Order> insertOrderFail(Order order) {

        UUID eventId = UUID.randomUUID();

        String payload = """
            {
              "orderId": "%s",
              "userId": "%s",
              "amount": %s
            }
            """.formatted(order.getId(), order.getUserId(), order.getAmount());

        Mono<Void> txFlow = databaseClient.sql(OrderQueries.INSERT)
                .bind("id", order.getId())
                .bind("userId", order.getUserId())
                .bind("status", order.getStatus())
                .bind("amount", order.getAmount())
                .bind("createdAt", order.getCreatedAt())
                .then()

                .then(
                        databaseClient.sql(OutboxQueries.INSERT)
                                .bind("id", eventId)
                                .bind("aggregateType", "ORDER")
                                .bind("aggregateId", order.getId())
                                .bind("eventType", "ORDER_CREATED")
                                .bind("payload", payload)
                                .bind("status", "PENDING")
                                .bind("createdAt", LocalDateTime.now())
                                .then()
                )
                .then(Mono.defer(() -> Mono.error(new RuntimeException("forced failure"))));
        return txOperator.transactional(txFlow)
                .thenReturn(order);
    }
}