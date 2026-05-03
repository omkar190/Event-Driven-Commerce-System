package com.order.service;

import com.order.service.entities.Order;
import com.order.service.repositories.OrderRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
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

    // Shared product ID for all tests
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();

        // Insert a test product before each test
        // so foreign key constraint is satisfied
        databaseClient.sql("""
                        INSERT INTO order_schema.products (id, name, available_qty, selling_price)
                        VALUES (:id, :name, :qty, :price)
                        ON CONFLICT (id) DO NOTHING
                        """)
                .bind("id", testProductId)
                .bind("name", "Test Product")
                .bind("qty", 100)
                .bind("price", BigDecimal.valueOf(99.99))
                .then()
                .block();
    }

    private Order buildOrder(String orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId("testUser");
        order.setProductId(testProductId);
        order.setQuantity(1);
        order.setAmount(BigDecimal.valueOf(99.99));
        order.setAddress("123 Test Street");
        order.setMobileNumber("+91-9876543210");
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    @Test
    void shouldSaveOrderAndOutboxTogether_whenSuccess() {

        String orderId = UUID.randomUUID().toString().replace("-", "");
        Order order = buildOrder(orderId);

        // ACT
        StepVerifier.create(repository.insertOrder(order))
                .expectNext(order)
                .verifyComplete();

        // VERIFY ORDER EXISTS
        StepVerifier.create(
                        databaseClient.sql("""
                                SELECT COUNT(*) FROM order_schema.orders WHERE id = :id
                                """)
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(1L)
                .verifyComplete();

        // VERIFY OUTBOX EXISTS
        StepVerifier.create(
                        databaseClient.sql("""
                                SELECT COUNT(*) FROM order_schema.order_outbox_event
                                WHERE aggregate_id = :id
                                """)
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(1L)
                .verifyComplete();

        // VERIFY EVENT TYPE
        StepVerifier.create(
                        databaseClient.sql("""
                                SELECT event_type
                                FROM order_schema.order_outbox_event
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
        Order order = buildOrder(orderId);

        StepVerifier.create(repository.insertOrderFail(order))
                .expectError(RuntimeException.class)
                .verify();

        // VERIFY ROLLBACK - order should NOT exist
        StepVerifier.create(
                        databaseClient.sql("""
                                SELECT COUNT(*) FROM order_schema.orders WHERE id = :id
                                """)
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(0L)
                .verifyComplete();

        // VERIFY ROLLBACK - outbox should NOT exist
        StepVerifier.create(
                        databaseClient.sql("""
                                SELECT COUNT(*) FROM order_schema.order_outbox_event
                                WHERE aggregate_id = :id
                                """)
                                .bind("id", orderId)
                                .map(row -> row.get(0, Long.class))
                                .one()
                )
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void shouldRollback_whenDatabaseConstraintFails() {

        String orderId = "23482129945e4ca88c8a45d962960639";
        Order order = buildOrder(orderId);

        // First insert should succeed
        StepVerifier.create(repository.insertOrder(order))
                .expectNext(order)
                .verifyComplete();

        // Second insert with same ID should fail (duplicate primary key)
        StepVerifier.create(repository.insertOrder(order))
                .expectError()
                .verify();

        // Only ONE record should exist
        StepVerifier.create(
                databaseClient.sql("""
                        SELECT COUNT(*) FROM order_schema.orders WHERE id = :id
                        """)
                        .bind("id", orderId)
                        .map(row -> row.get(0, Long.class))
                        .one()
        ).expectNext(1L).verifyComplete();
    }
}