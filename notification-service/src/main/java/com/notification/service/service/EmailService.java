package com.notification.service.service;

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

        String htmlBody = buildEmailBody(event);

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

            System.out.println("Email sent successfully: " + response);
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
        }
    }

    private String buildEmailBody(OtpNotificationEvent event) {
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
}