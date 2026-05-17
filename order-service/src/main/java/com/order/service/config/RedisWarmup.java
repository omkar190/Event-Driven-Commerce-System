package com.order.service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisWarmup {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public RedisWarmup(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @PostConstruct
    public void warmup() {
        reactiveRedisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .ping()
                .doOnSuccess(result -> System.out.println("Redis connection warmed up: " + result))
                .doOnError(e -> System.err.println("Redis warmup failed: " + e.getMessage()))
                .subscribe();
    }
}