package com.order.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.service.entities.User;
import com.order.service.repositories.UserRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserCacheService {

    private static final String USER_KEY_PREFIX = "users:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public UserCacheService(UserRepository userRepository, ReactiveRedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<User> findByEmail(String email) {
        String key = USER_KEY_PREFIX + email;

        return redisTemplate.opsForValue()
                .get(key)
                .defaultIfEmpty("__NULL__")
                .flatMap(json -> {
                    if ("__NULL__".equals(json)) {
                        System.out.println("Cache MISS: " + key);
                        return userRepository.findByEmail(email)
                                .flatMap(user -> {
                                    try {
                                        String serialized = objectMapper.writeValueAsString(user);
                                        return redisTemplate.opsForValue()
                                                .set(key, serialized, TTL)
                                                .doOnSuccess(r -> System.out.println("User cached: " + key))
                                                .doOnError(e -> System.err.println("Redis SET error: " + e.getMessage()))
                                                .thenReturn(user);
                                    } catch (Exception e) {
                                        return Mono.just(user);
                                    }
                                });
                    } else {
                        System.out.println("Cache HIT: " + key);
                        try {
                            return Mono.just(objectMapper.readValue(json, User.class));
                        } catch (Exception e) {
                            System.err.println("Deserialization error: " + e.getMessage());
                            return Mono.<User>empty();
                        }
                    }
                });
    }

    public Mono<Void> evictUserCache(String email) {
        return redisTemplate.delete(USER_KEY_PREFIX + email)
                .doOnSuccess(count -> System.out.println("Evicted user cache: " + email))
                .then();
    }
}