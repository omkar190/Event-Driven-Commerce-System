package com.order.service.repositories;

import com.order.service.entities.Order;
import com.order.service.mapper.OrderRowMapper;
import com.order.service.queries.OrderQueries;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class OrderRepositoryImpl implements CustomOrderRepository {

    private final DatabaseClient databaseClient;
    private final OrderRowMapper mapper = new OrderRowMapper();

    public OrderRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Order> insertOrder(Order order) {

        return databaseClient.sql(OrderQueries.INSERT)
                .bind("id", order.getId())
                .bind("userId", order.getUserId())
                .bind("status", order.getStatus())
                .bind("amount", order.getAmount())
                .bind("createdAt", order.getCreatedAt())
                .map(mapper::apply)
                .one();
    }
}