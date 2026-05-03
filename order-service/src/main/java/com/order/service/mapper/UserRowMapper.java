package com.order.service.mapper;

import com.order.service.entities.User;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class UserRowMapper implements BiFunction<Row, RowMetadata, User> {

    @Override
    public User apply(Row row, RowMetadata metadata) {
        User user = new User();
        user.setId(row.get("id", UUID.class));
        user.setEmail(row.get("email", String.class));
        user.setPassword(row.get("password", String.class));
        user.setCountryCode(row.get("country_code", String.class));
        user.setMobileNumber(row.get("mobile_number", String.class));
        user.setRole(row.get("role", String.class));
        user.setActive(Boolean.TRUE.equals(row.get("is_active", Boolean.class)));
        user.setCreatedAt(row.get("created_at", LocalDateTime.class));
        return user;
    }
}