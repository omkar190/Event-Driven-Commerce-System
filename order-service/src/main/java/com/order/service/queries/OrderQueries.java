package com.order.service.queries;

public class OrderQueries {

    private OrderQueries() {}

    public static final String INSERT = """
            INSERT INTO order_schema.orders (id, user_id, product_id, quantity, amount, address, mobile_number, status, created_at)
            VALUES (:id, :userId, :productId, :quantity, :amount, :address, :mobileNumber, :status, :createdAt)
            """;

    public static final String UPDATE_STATUS = """
                UPDATE order_schema.orders SET status = 'SENT' WHERE id = :id
                """;
}
