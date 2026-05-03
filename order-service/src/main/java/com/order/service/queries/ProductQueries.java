package com.order.service.queries;

public class ProductQueries {

    public static final String FIND_ALL = """
            SELECT * FROM order_schema.products
            ORDER BY name
            """;

    public static final String FIND_BY_ID = """
            SELECT * FROM order_schema.products WHERE id = :id
            """;

    public static final String REDUCE_STOCK = """
            UPDATE order_schema.products
            SET available_qty = available_qty - :quantity
            WHERE id = :id AND available_qty >= :quantity
            """;
}