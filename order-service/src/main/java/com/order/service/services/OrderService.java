package com.order.service.services;

import com.order.service.dto.OrderRequest;
import com.order.service.entities.Order;
import com.order.service.repositories.CustomOrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private final CustomOrderRepository orderRepository;
    private final ProductCacheService productCacheService;
    private final StripeService stripeService;

    public OrderService(CustomOrderRepository orderRepository,
                        ProductCacheService productCacheService,
                        StripeService stripeService) {
        this.orderRepository = orderRepository;
        this.productCacheService = productCacheService;
        this.stripeService = stripeService;
    }

    public Mono<Order> createOrder(OrderRequest request) {
        return productCacheService.getProductById(request.getProductId())
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
                .flatMap(product -> {
                    if (product.getAvailableQty() < request.getQuantity()) {
                        return Mono.error(new RuntimeException(
                                "Insufficient stock. Available: " + product.getAvailableQty()));
                    }

                    BigDecimal amount = product.getSellingPrice()
                            .multiply(BigDecimal.valueOf(request.getQuantity()));

                    String clientSecret = stripeService.createPaymentIntent(amount);

                    Order order = new Order();
                    order.setId(String.valueOf(UUID.randomUUID()));
                    order.setUserId(request.getUserId());
                    order.setProductId(request.getProductId());
                    order.setQuantity(request.getQuantity());
                    order.setAmount(amount);
                    order.setAddress(request.getAddress());
                    order.setStatus("PENDING_PAYMENT");
                    order.setCreatedAt(LocalDateTime.now());
                    order.setMobileNumber(request.getMobileNumber());
                    order.setStripeClientSecret(clientSecret);

                    return orderRepository.insertOrder(order);
                });
    }

    public Mono<Order> verifyPaymentAndConfirm(String orderId, String paymentIntentId) {
        boolean isValid = stripeService.verifyPaymentIntent(paymentIntentId);

        if (!isValid) {
            return Mono.error(new RuntimeException("Payment not completed"));
        }

        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order ->
                        productCacheService.getProductById(order.getProductId())
                                .map(product -> product.getName())
                                .defaultIfEmpty("Unknown Product")
                                .flatMap(productName ->
                                        orderRepository.confirmPayment(orderId, paymentIntentId, productName)
                                )
                                .flatMap(confirmedOrder ->
                                        productCacheService.evictProductCache(confirmedOrder.getProductId())
                                                .thenReturn(confirmedOrder)
                                )
                );
    }

    public Mono<Order> handlePaymentFailure(String orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order ->
                        productCacheService.getProductById(order.getProductId())
                                .map(product -> product.getName())
                                .defaultIfEmpty("Unknown Product")
                                .flatMap(productName ->
                                        orderRepository.failPayment(orderId, productName)
                                )
                );
    }
}