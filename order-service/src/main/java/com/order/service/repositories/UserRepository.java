package com.order.service.repositories;

import com.order.service.entities.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}