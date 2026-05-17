package com.order.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentVerificationRequest {

    private String paymentIntentId;

}