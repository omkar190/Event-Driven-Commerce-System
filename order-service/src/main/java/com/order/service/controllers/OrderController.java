package com.order.service.controllers;

import com.order.service.entities.Order;
import com.order.service.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public Mono<Order> create(@Valid @RequestBody Order order){
        return orderService.createOrder(order);
    }

}
