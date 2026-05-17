package com.order.service.repositories;

import com.order.service.entities.Order;
import reactor.core.publisher.Mono;

public interface CustomOrderRepository {
    Mono<Order> insertOrder(Order order);
    Mono<Order> findById(String orderId);
    Mono<Order> confirmPayment(String orderId, String stripePaymentIntentId, String productName);
    Mono<Order> failPayment(String orderId, String productName);
}
