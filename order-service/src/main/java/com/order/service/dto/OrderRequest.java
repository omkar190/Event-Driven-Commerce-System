package com.order.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderRequest {

    private String userId;
    private UUID productId;
    private int quantity;
    private String address;
    private String mobileNumber;
}