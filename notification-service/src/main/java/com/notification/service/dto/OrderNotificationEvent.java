package com.notification.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderNotificationEvent {
    private String email;
    private String type;
    private String orderId;
    private String productName;
    private int quantity;
    private String amount;
    private String address;

    public OrderNotificationEvent() {}

}