package com.notification.service.service;

import com.notification.service.dto.OrderNotificationEvent;
import com.notification.service.dto.OtpNotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestClient restClient;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .build();
    }

    public void sendOtpEmail(OtpNotificationEvent event) {
        String subject = event.getType().equals("SIGNUP")
                ? "Verify your account - OTP"
                : "Your login OTP";

        String htmlBody = buildOtpEmailBody(event);

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(event.getEmail()),
                "subject", subject,
                "html", htmlBody
        );

        try {
            String response = restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            System.out.println("OTP email sent successfully: " + response);
        } catch (Exception e) {
            System.err.println("OTP email send failed: " + e.getMessage());
        }
    }

    public void sendOrderEmail(OrderNotificationEvent event) {
        boolean isSuccess = "ORDER_SUCCESS".equals(event.getType());

        String subject = isSuccess
                ? "Order Confirmed - " + event.getOrderId()
                : "Order Failed - " + event.getOrderId();

        String htmlBody = buildOrderEmailBody(event, isSuccess);

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", List.of(event.getEmail()),
                "subject", subject,
                "html", htmlBody
        );

        try {
            String response = restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            System.out.println("Order email sent successfully: " + response);
        } catch (Exception e) {
            System.err.println("Order email send failed: " + e.getMessage());
        }
    }

    private String buildOtpEmailBody(OtpNotificationEvent event) {
        String action = event.getType().equals("SIGNUP")
                ? "verify your account"
                : "login to your account";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 500px; margin: 40px auto; background: white; border-radius: 12px; padding: 40px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                        .header { text-align: center; margin-bottom: 30px; }
                        .otp-box { background: #6366f1; color: white; font-size: 36px; font-weight: bold; text-align: center; padding: 20px; border-radius: 12px; letter-spacing: 8px; margin: 30px 0; }
                        .footer { text-align: center; color: #888; font-size: 12px; margin-top: 30px; }
                        .warning { color: #ef4444; font-size: 13px; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🛒 Order Service</h2>
                            <p>Use the OTP below to %s</p>
                        </div>
                        <div class="otp-box">%s</div>
                        <p class="warning">⏱ This OTP is valid for <strong>10 minutes</strong> only.</p>
                        <p class="warning">If you did not request this, please ignore this email.</p>
                        <div class="footer">
                            <p>© 2024 Order Service. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(action, event.getOtp());
    }

    private String buildOrderEmailBody(OrderNotificationEvent event, boolean isSuccess) {
        String statusColor = isSuccess ? "#22c55e" : "#ef4444";
        String statusIcon = isSuccess ? "✅" : "❌";
        String statusText = isSuccess ? "Order Confirmed" : "Payment Failed";
        String message = isSuccess
                ? "Your payment was successful and your order has been confirmed."
                : "Unfortunately, your payment could not be processed. No amount has been charged.";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 500px; margin: 40px auto; background: white; border-radius: 12px; padding: 40px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                        .header { text-align: center; margin-bottom: 20px; }
                        .status-badge { background: %s; color: white; padding: 12px 24px; border-radius: 8px; font-size: 18px; font-weight: bold; text-align: center; margin: 20px 0; }
                        .details { background: #f8f9fa; border-radius: 8px; padding: 20px; margin: 20px 0; }
                        .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e5e7eb; }
                        .detail-row:last-child { border-bottom: none; }
                        .detail-label { color: #6b7280; font-size: 14px; }
                        .detail-value { font-weight: bold; font-size: 14px; }
                        .message { text-align: center; color: #555; margin: 20px 0; }
                        .footer { text-align: center; color: #888; font-size: 12px; margin-top: 30px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🛒 Order Service</h2>
                        </div>
                        <div class="status-badge">%s %s</div>
                        <p class="message">%s</p>
                        <div class="details">
                            <div class="detail-row">
                                <span class="detail-label">Order ID&nbsp;:&nbsp;</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Product&nbsp;:&nbsp;</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Quantity&nbsp;:&nbsp;</span>
                                <span class="detail-value">%d</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Amount&nbsp;:&nbsp;</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Delivery Address&nbsp;:&nbsp;</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2024 Order Service. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                statusColor, statusIcon, statusText, message,
                 event.getOrderId(), event.getProductName(),
                event.getQuantity(), event.getAmount(), event.getAddress()
        );
    }
}