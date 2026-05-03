package com.order.service.services;

import com.order.service.dto.OrderRequest;
import com.order.service.entities.Order;
import com.order.service.repositories.OrderRepository;
import com.order.service.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ProductCacheService productCacheService){
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productCacheService = productCacheService;
    }

    public Mono<Order> createOrder(OrderRequest request){
        return productCacheService.getProductById(request.getProductId())
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
                .flatMap(product -> {
                    if (product.getAvailableQty() < request.getQuantity()) {
                        return Mono.error(new RuntimeException(
                                "Insufficient stock. Available: " + product.getAvailableQty()));
                    }

                    BigDecimal amount = product.getSellingPrice()
                            .multiply(BigDecimal.valueOf(request.getQuantity()));

                    Order order = new Order();
                    order.setId(String.valueOf(UUID.randomUUID()));
                    order.setUserId(request.getUserId());
                    order.setProductId(request.getProductId());
                    order.setQuantity(request.getQuantity());
                    order.setAmount(amount);
                    order.setAddress(request.getAddress());
                    order.setStatus("CREATED");
                    order.setCreatedAt(LocalDateTime.now());
                    order.setMobileNumber(request.getMobileNumber());

                    return orderRepository.insertOrder(order)
                            // Evict cache after order placed (stock changed)
                            .flatMap(savedOrder ->
                                    productCacheService.evictProductCache(request.getProductId())
                                            .thenReturn(savedOrder)
                            );
                });
    }

}
