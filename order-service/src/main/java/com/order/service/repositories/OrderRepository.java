package com.order.service.repositories;

import com.order.service.entities.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, String>, CustomOrderRepository {

    public Mono<Order> insertOrder(Order order);

}
