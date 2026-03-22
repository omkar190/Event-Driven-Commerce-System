package com.order.service.repositories;

import com.order.service.entities.Order;
import reactor.core.publisher.Mono;

public interface CustomOrderRepository {
    Mono<Order> insertOrder(Order order);
}
