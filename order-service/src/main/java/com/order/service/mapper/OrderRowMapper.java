package com.order.service.mapper;

import com.order.service.entities.Order;
import io.r2dbc.spi.Row;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

public class OrderRowMapper implements BiFunction<Row, Object, Order> {

    @Override
    public Order apply(Row row, Object o) {

        Order order = new Order();

        order.setId(row.get("id", String.class));
        order.setUserId(row.get("user_id", String.class));
        order.setStatus(row.get("status", String.class));
        order.setAmount(row.get("amount", Double.class));
        order.setCreatedAt(row.get("created_at", LocalDateTime.class));

        return order;
    }
}