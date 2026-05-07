package com.order.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.service.config.FeatureFlags;
import com.order.service.dto.OtpNotificationEvent;
import com.order.service.entities.User;
import com.order.service.repositories.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.order.service.config.RabbitMQConfig.NOTIFICATION_EXCHANGE;
import static com.order.service.config.RabbitMQConfig.NOTIFICATION_ROUTING_KEY;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final OtpService otpService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FeatureFlags featureFlags;

    public AuthService(UserRepository userRepository,
                       UserCacheService userCacheService,
                       OtpService otpService,
                       RabbitTemplate rabbitTemplate,
                       ObjectMapper objectMapper,
                       FeatureFlags featureFlags) {
        this.userRepository = userRepository;
        this.userCacheService = userCacheService;
        this.otpService = otpService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.featureFlags = featureFlags;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // SIGNUP
    public Mono<String> signUp(String email, String password,
                               String countryCode, String mobileNumber) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return userRepository.findByEmail(email)
                                .flatMap(existingUser -> {
                                    if (!existingUser.isActive()) {
                                        if (!featureFlags.isOtpEnabled()) {
                                            // OTP OFF - just activate directly
                                            return userRepository.activateUser(email)
                                                    .then(userCacheService.evictUserCache(email))
                                                    .thenReturn("Account activated. Please login.");
                                        }
                                        // OTP ON - resend OTP
                                        return sendOtpEvent(email, "SIGNUP")
                                                .thenReturn("Account already registered but not verified. " +
                                                        "A new OTP has been sent to " + email);
                                    }
                                    return Mono.error(
                                            new RuntimeException("Email already exists. Please login.")
                                    );
                                });
                    }

                    User user = new User(email,
                            passwordEncoder.encode(password),
                            countryCode,
                            mobileNumber);

                    if (featureFlags.isOtpEnabled()) {
                        // OTP ON - save as inactive, send OTP
                        user.setActive(false);
                        return userRepository.save(user)
                                .flatMap(savedUser -> sendOtpEvent(email, "SIGNUP")
                                        .thenReturn("OTP sent to " + email + ". Valid for 10 minutes.")
                                );
                    } else {
                        // OTP OFF - save as active directly, no OTP
                        user.setActive(true);
                        return userRepository.save(user)
                                .thenReturn("Account created successfully. Please login.");
                    }
                });
    }

    // VERIFY SIGNUP OTP
    public Mono<String> verifySignupOtp(String email, String otp) {
        if (!featureFlags.isOtpEnabled()) {
            return Mono.error(new RuntimeException("OTP verification is disabled"));
        }
        return otpService.verifyOtp(email, "SIGNUP", otp)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new RuntimeException("Invalid or expired OTP"));
                    }
                    return userRepository.activateUser(email)
                            .then(userCacheService.evictUserCache(email))
                            .thenReturn("Account activated! Please login.");
                });
    }

    // LOGIN
    public Mono<String> login(String email, String password) {
        return userCacheService.findByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.error(new RuntimeException(
                                "Account not activated. Please verify your email."));
                    }
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new RuntimeException("Invalid password"));
                    }

                    if (featureFlags.isOtpEnabled()) {
                        // OTP ON - send OTP
                        return sendOtpEvent(email, "LOGIN")
                                .thenReturn("OTP sent to " + email + ". Valid for 10 minutes.");
                    } else {
                        // OTP OFF - return success directly
                        return Mono.just("LOGIN_SUCCESS:" + email);
                    }
                });
    }

    // VERIFY LOGIN OTP
    public Mono<User> verifyLoginOtp(String email, String otp) {
        if (!featureFlags.isOtpEnabled()) {
            return Mono.error(new RuntimeException("OTP verification is disabled"));
        }
        return otpService.verifyOtp(email, "LOGIN", otp)
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new RuntimeException("Invalid or expired OTP"));
                    }
                    return userCacheService.findByEmail(email);
                });
    }

    // RESEND OTP
    public Mono<String> resendOtp(String email, String type) {
        if (!featureFlags.isOtpEnabled()) {
            return Mono.error(new RuntimeException("OTP is disabled"));
        }
        return sendOtpEvent(email, type)
                .thenReturn("OTP resent to " + email + ". Valid for 10 minutes.");
    }

    // Private helper to send OTP event
    private Mono<Void> sendOtpEvent(String email, String type) {
        return otpService.generateAndStoreOtp(email, type)
                .flatMap(otp ->
                        Mono.fromCallable(() -> {
                                    OtpNotificationEvent event =
                                            new OtpNotificationEvent(email, otp, type);
                                    String payload = objectMapper.writeValueAsString(event);
                                    rabbitTemplate.convertAndSend(
                                            NOTIFICATION_EXCHANGE,
                                            NOTIFICATION_ROUTING_KEY,
                                            payload
                                    );
                                    System.out.println("OTP sent for " + type + " -> " + email);
                                    return null;
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                                .then()
                );
    }
}