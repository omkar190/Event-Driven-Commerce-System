package com.order.service.services;

import com.order.service.entities.Order;
import com.order.service.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    public Mono<Order> createOrder(Order order){
        order.setStatus("CREATED");
        order.setId(UUID.randomUUID().toString().replace("-",""));
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.insertOrder(order);
    }

}
