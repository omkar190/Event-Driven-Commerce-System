package com.order.service.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class User {

    private UUID id;
    private String email;
    private String password;
    private String countryCode;
    private String mobileNumber;
    private String role;
    private boolean isActive;
    private LocalDateTime createdAt;

    public User(){}

    public User(String email, String password, String countryCode, String mobileNumber) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.countryCode = countryCode;
        this.mobileNumber = mobileNumber;
        this.role = "USER";
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
}