package com.order.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderNotificationEvent {
    private String email;
    private String type; // ORDER_SUCCESS or ORDER_FAILED
    private String orderId;
    private String productName;
    private int quantity;
    private String amount;
    private String address;

    public OrderNotificationEvent() {}

    public OrderNotificationEvent(String email, String type, String orderId,
                                  String productName, int quantity,
                                  String amount, String address) {
        this.email = email;
        this.type = type;
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.amount = amount;
        this.address = address;
    }

}