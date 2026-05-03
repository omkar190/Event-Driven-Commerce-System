package com.order.service.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class AuthResponse {
    private UUID id;
    private String email;
    private String role;
    private String message;
    private String countryCode;
    private String mobileNumber;

    public AuthResponse(UUID id, String email, String role, String message, String countryCode, String mobileNumber) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.message = message;
        this.countryCode = countryCode;
        this.mobileNumber = mobileNumber;
    }

}