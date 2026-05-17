package com.order.service.mapper;

import com.order.service.entities.Order;
import io.r2dbc.spi.Row;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class OrderRowMapper implements BiFunction<Row, Object, Order> {

    @Override
    public Order apply(Row row, Object o) {

        Order order = new Order();
        order.setId(row.get("id", String.class));
        order.setUserId(row.get("user_id", String.class));
        order.setProductId(UUID.fromString(row.get("product_id", UUID.class).toString()));
        order.setQuantity(row.get("quantity", Integer.class));
        order.setAmount(row.get("amount", BigDecimal.class));
        order.setAddress(row.get("address", String.class));
        order.setMobileNumber(row.get("mobile_number", String.class));
        order.setStatus(row.get("status", String.class));
        order.setCreatedAt(row.get("created_at", LocalDateTime.class));
        order.setStripeClientSecret(row.get("stripe_client_secret", String.class));
        order.setStripePaymentIntentId(row.get("stripe_payment_intent_id", String.class));

        return order;
    }
}