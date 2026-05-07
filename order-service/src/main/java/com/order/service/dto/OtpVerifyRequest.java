package com.order.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequest {

    private String email;
    private String otp;
    private String type; // "SIGNUP" or "LOGIN" (for resend)

}