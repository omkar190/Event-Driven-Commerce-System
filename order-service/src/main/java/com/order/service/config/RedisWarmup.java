package com.order.service.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RedisWarmup {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public RedisWarmup(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {

        try {

            reactiveRedisTemplate.getConnectionFactory()
                    .getReactiveConnection()
                    .ping()
                    .timeout(Duration.ofSeconds(5))
                    .doOnSuccess(result ->
                            System.out.println("Redis warmed up: " + result))
                    .doOnError(error ->
                            System.out.println("Redis warmup failed: " + error.getMessage()))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe();

        } catch (Exception e) {

            System.out.println("Redis startup warmup exception: " + e.getMessage());
        }
    }
}