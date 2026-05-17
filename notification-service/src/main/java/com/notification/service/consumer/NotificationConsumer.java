package com.notification.service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.service.dto.OrderNotificationEvent;
import com.notification.service.dto.OtpNotificationEvent;
import com.notification.service.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "notification.queue")
    public void handleOtpNotification(String payload) {
        try {
            OtpNotificationEvent event =
                    objectMapper.readValue(payload, OtpNotificationEvent.class);

            System.out.println("Received OTP notification for: " + event.getEmail()
                    + " | Type: " + event.getType());

            emailService.sendOtpEmail(event);

        } catch (Exception e) {
            System.err.println("Failed to parse OTP event: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "notification.order.queue")
    public void handleOrderNotification(String payload) {
        try {
            OrderNotificationEvent event =
                    objectMapper.readValue(payload, OrderNotificationEvent.class);

            System.out.println("Received order notification for: " + event.getEmail()
                    + " | Type: " + event.getType());

            emailService.sendOrderEmail(event);

        } catch (Exception e) {
            System.err.println("Failed to parse order event: " + e.getMessage());
        }
    }

}