package com.notification.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpNotificationEvent {

    private String email;
    private String otp;
    private String type; // "SIGNUP" or "LOGIN"
    private String name;

    public OtpNotificationEvent() {}

}