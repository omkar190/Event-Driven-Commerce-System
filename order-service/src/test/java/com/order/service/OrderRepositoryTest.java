package com.order.service;

import com.order.service.entities.Order;
import com.order.service.repositories.OrderRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepositoryImpl repository;

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void shouldSaveOrderAndOutboxTogether_whenSuccess() {

        Order order = new Order();

        String orderId = UUID.randomUUID().toString().replace("-", "");
        order.setId(orderId);
        order.setUserId("testUser");
        order.setStatus("CREATED");
        order.setAmount(BigDecimal.valueOf(100.0));
        order.setCreatedAt(LocalDateTime.now());

        // ACT
        StepVerifier.create(repository.insertOrder(order))
                .expectNext(order)
                .verifyComplete();

        // VERIFY ORDER EXISTS
        StepVerifier.create(
                        databaseClient.sql("SELECT COUNT(*) FROM orders WHERE id = :id")
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(1L)
                .verifyComplete();

        // VERIFY OUTBOX EXISTS (linked by aggregate_id = orderId)
        StepVerifier.create(
                        databaseClient.sql("SELECT COUNT(*) FROM order_outbox_event WHERE aggregate_id = :id")
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(1L)
                .verifyComplete();

        // verify event exists with correct type
        StepVerifier.create(
                        databaseClient.sql("""
                        SELECT event_type 
                        FROM order_outbox_event 
                        WHERE aggregate_id = :id
                        """)
                                .bind("id", orderId)
                                .map(row -> row.get("event_type", String.class))
                                .one()
                )
                .expectNext("ORDER_CREATED")
                .verifyComplete();
    }

    @Test
    void shouldRollback_whenExceptionThrownAfterInsert() {

        String orderId = UUID.randomUUID().toString();

        Order order = new Order();
        order.setId(orderId);
        order.setUserId("testUser");
        order.setStatus("CREATED");
        order.setAmount(BigDecimal.valueOf(100.0));
        order.setCreatedAt(LocalDateTime.now());

        StepVerifier.create(repository.insertOrderFail(order))
                .expectError(RuntimeException.class)
                .verify();

        // VERIFY ROLLBACK (nothing should exist)
        StepVerifier.create(
                        databaseClient.sql("SELECT COUNT(*) FROM orders WHERE id = :id")
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(
                        databaseClient.sql("SELECT COUNT(*) FROM order_outbox_event WHERE aggregate_id = :id")
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void shouldRollback_whenDatabaseConstraintFails() {

        Order order = new Order();
        order.setId("23482129945e4ca88c8a45d962960639"); // force unique violation
        order.setUserId("testUser");
        order.setStatus("CREATED");
        order.setAmount(BigDecimal.valueOf(100.0));
        order.setCreatedAt(LocalDateTime.now());

        StepVerifier.create(repository.insertOrder(order))
                .expectNext(order)
                .verifyComplete();

        StepVerifier.create(repository.insertOrder(order))
                .expectError()
                .verify();

        StepVerifier.create(
                databaseClient.sql("SELECT COUNT(*) FROM orders WHERE id = :id")
                        .bind("id", "23482129945e4ca88c8a45d962960639")
                        .map(row -> row.get(0, Long.class))
                        .one()
        ).expectNext(1L).verifyComplete();
    }
}
