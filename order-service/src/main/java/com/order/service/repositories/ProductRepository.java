package com.order.service.repositories;

import com.order.service.entities.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository {
    Flux<Product> findAll();
    Mono<Product> findById(UUID id);
    Mono<Long> reduceStock(UUID id, int quantity);
}