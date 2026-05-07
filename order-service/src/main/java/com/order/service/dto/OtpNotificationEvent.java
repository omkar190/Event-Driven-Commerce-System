package com.order.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpNotificationEvent {

    private String email;
    private String otp;
    private String type; // "SIGNUP" or "LOGIN"

    public OtpNotificationEvent() {}

    public OtpNotificationEvent(String email, String otp, String type) {
        this.email = email;
        this.otp = otp;
        this.type = type;
    }

}