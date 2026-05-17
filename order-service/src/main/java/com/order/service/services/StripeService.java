package com.order.service.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public String createPaymentIntent(BigDecimal amount) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

    public boolean verifyPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(intent.getStatus());
        } catch (StripeException e) {
            return false;
        }
    }
}