package com.order.service.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AuthResponse {
    private UUID id;
    private String email;
    private String role;
    private String message;

    public AuthResponse(UUID id, String email, String role, String message) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.message = message;
    }

}