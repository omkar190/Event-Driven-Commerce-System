package com.order.service.mapper;

import com.order.service.entities.Product;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class ProductRowMapper implements BiFunction<Row, RowMetadata, Product> {

    @Override
    public Product apply(Row row, RowMetadata metadata) {
        Product product = new Product();
        product.setId(row.get("id", UUID.class));
        product.setName(row.get("name", String.class));
        product.setAvailableQty(row.get("available_qty", Integer.class));
        product.setSellingPrice(row.get("selling_price", BigDecimal.class));
        product.setCreatedAt(row.get("created_at", LocalDateTime.class));
        return product;
    }
}