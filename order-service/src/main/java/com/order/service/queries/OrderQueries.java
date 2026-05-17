package com.order.service.queries;

public class OrderQueries {

    public static final String INSERT = """
            INSERT INTO order_schema.orders (id, user_id, product_id, quantity, amount, address, mobile_number, status, created_at, stripe_client_secret)
            VALUES (:id, :userId, :productId, :quantity, :amount, :address, :mobileNumber, :status, :createdAt, :stripeClientSecret)
            """;

    public static final String UPDATE_STATUS = """
            UPDATE order_schema.orders SET status = 'SENT' WHERE id = :id
            """;

    public static final String FIND_BY_ID = """
            SELECT id, user_id, product_id, quantity, amount, address, mobile_number, status, created_at, stripe_client_secret, stripe_payment_intent_id
            FROM order_schema.orders WHERE id = :id
            """;

    public static final String UPDATE_PAYMENT_SUCCESS = """
            UPDATE order_schema.orders
            SET status = 'CONFIRMED', stripe_payment_intent_id = :stripePaymentIntentId
            WHERE id = :id AND status IN ('PENDING_PAYMENT', 'SENT')
            """;

    public static final String UPDATE_PAYMENT_FAILED = """
            UPDATE order_schema.orders
            SET status = 'PAYMENT_FAILED'
            WHERE id = :id AND status IN ('PENDING_PAYMENT', 'SENT')
            """;
}