package com.order.service.queries;

public class OrderQueries {

    private OrderQueries() {}

    public static final String INSERT = """
                INSERT INTO order_schema.orders (id, user_id, status, amount, created_at)
                VALUES (:id, :userId, :status, :amount, :createdAt)
                RETURNING id, user_id, status, amount, created_at
                """;

}
