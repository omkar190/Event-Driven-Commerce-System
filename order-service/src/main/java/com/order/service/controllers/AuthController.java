package com.order.service.controllers;

import com.order.service.dto.AuthRequest;
import com.order.service.dto.AuthResponse;
import com.order.service.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponse>> signUp(@RequestBody AuthRequest request) {
        return authService.signUp(request.getEmail(), request.getPassword(),request.getCountryCode(), request.getMobileNumber())
                .map(user -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new AuthResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getRole(),
                                "Account created successfully",
                                user.getCountryCode(),
                                user.getMobileNumber()
                        ))
                )
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(new AuthResponse(null, null, null, e.getMessage(), null, null))
                ));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return authService.login(request.getEmail(), request.getPassword())
                .map(user -> ResponseEntity.ok(
                        new AuthResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getRole(),
                                "Login successful",
                                user.getCountryCode(),
                                user.getMobileNumber()
                        ))
                )
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new AuthResponse(null, null, null, e.getMessage(), null, null))
                ));
    }
}