package com.order.service.services;

import com.order.service.entities.User;
import com.order.service.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Mono<User> signUp(String email, String password, String countryCode, String mobileNumber) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Email already exists"));
                    }
                    User user = new User(email, passwordEncoder.encode(password), countryCode, mobileNumber);
                    return userRepository.save(user);
                });
    }

    public Mono<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.error(new RuntimeException("Account is deactivated"));
                    }
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new RuntimeException("Invalid password"));
                    }
                    return Mono.just(user);
                });
    }
}