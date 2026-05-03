package com.order.service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.service.entities.Product;
import com.order.service.repositories.ProductRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ProductCacheService {

    private static final String ALL_PRODUCTS_KEY = "products:all";
    private static final String PRODUCT_KEY_PREFIX = "products:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ProductRepository productRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ProductCacheService(ProductRepository productRepository, ReactiveRedisTemplate<String, String> redisTemplate,
                               ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Flux<Product> getAllProducts() {
        return redisTemplate.opsForValue()
                .get(ALL_PRODUCTS_KEY)
                .defaultIfEmpty("__NULL__")  // Prevent empty Mono
                .flatMapMany(json -> {
                    if ("__NULL__".equals(json)) {
                        // Cache MISS - fetch from DB
                        System.out.println("Cache MISS: fetching from DB");
                        return productRepository.findAll()
                                .collectList()
                                .flatMapMany(products -> {
                                    try {
                                        String serialized = objectMapper.writeValueAsString(products);
                                        return redisTemplate.opsForValue()
                                                .set(ALL_PRODUCTS_KEY, serialized, TTL)
                                                .doOnSuccess(r -> System.out.println("Cached " + products.size() + " products. Result: " + r))
                                                .doOnError(e -> System.err.println("Redis SET error: " + e.getMessage()))
                                                .thenMany(Flux.fromIterable(products));
                                    } catch (Exception e) {
                                        System.err.println("Serialization error: " + e.getMessage());
                                        return Flux.fromIterable(products);
                                    }
                                });
                    } else {
                        // Cache HIT
                        System.out.println("Cache HIT: products:all");
                        try {
                            List<Product> products = objectMapper.readValue(
                                    json, new TypeReference<List<Product>>() {});
                            return Flux.fromIterable(products);
                        } catch (Exception e) {
                            System.err.println("Deserialization error: " + e.getMessage());
                            return Flux.<Product>empty();
                        }
                    }
                });
    }

    public Mono<Product> getProductById(UUID id) {
        String key = PRODUCT_KEY_PREFIX + id;

        return redisTemplate.opsForValue()
                .get(key)
                .defaultIfEmpty("__NULL__")
                .flatMap(json -> {
                    if ("__NULL__".equals(json)) {
                        System.out.println("Cache MISS: " + key);
                        return productRepository.findById(id)
                                .flatMap(product -> {
                                    try {
                                        String serialized = objectMapper.writeValueAsString(product);
                                        return redisTemplate.opsForValue()
                                                .set(key, serialized, TTL)
                                                .doOnSuccess(r -> System.out.println("Product cached: " + key))
                                                .doOnError(e -> System.err.println("Redis SET error: " + e.getMessage()))
                                                .thenReturn(product);
                                    } catch (Exception e) {
                                        return Mono.just(product);
                                    }
                                });
                    } else {
                        System.out.println("Cache HIT: " + key);
                        try {
                            return Mono.just(objectMapper.readValue(json, Product.class));
                        } catch (Exception e) {
                            System.err.println("Deserialization error: " + e.getMessage());
                            return Mono.<Product>empty();
                        }
                    }
                });
    }

    public Mono<Void> evictProductCache(UUID id) {
        return redisTemplate.delete(ALL_PRODUCTS_KEY, PRODUCT_KEY_PREFIX + id)
                .doOnSuccess(count -> System.out.println("Evicted " + count + " keys"))
                .then();
    }
}