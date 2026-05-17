package com.order.service.controllers;

import com.order.service.dto.OrderRequest;
import com.order.service.dto.PaymentVerificationRequest;
import com.order.service.entities.Order;
import com.order.service.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> create(@RequestBody OrderRequest request) {
        return orderService.createOrder(request)
                .map(order -> ResponseEntity.ok().body((Object) order))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body((Object) Map.of("message", e.getMessage()))));
    }

    @PostMapping("/{orderId}/verify-payment")
    public Mono<ResponseEntity<Object>> verifyPayment(
            @PathVariable String orderId,
            @RequestBody PaymentVerificationRequest request) {
        return orderService.verifyPaymentAndConfirm(orderId, request.getPaymentIntentId())
                .map(order -> ResponseEntity.ok()
                        .body((Object) Map.of(
                                "message", "Payment verified and order confirmed",
                                "orderId", order.getId(),
                                "status", order.getStatus()
                        )))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body((Object) Map.of("message", e.getMessage()))));
    }

    @PostMapping("/{orderId}/payment-failed")
    public Mono<ResponseEntity<Object>> paymentFailed(@PathVariable String orderId) {
        return orderService.handlePaymentFailure(orderId)
                .map(order -> ResponseEntity.ok()
                        .body((Object) Map.of(
                                "message", "Order marked as failed",
                                "orderId", order.getId()
                        )))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body((Object) Map.of("message", e.getMessage()))));
    }
}