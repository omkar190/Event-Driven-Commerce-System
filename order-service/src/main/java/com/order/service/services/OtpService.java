package com.order.service.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@Service
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final String OTP_PREFIX = "otp:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public OtpService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Generate and store OTP
    public Mono<String> generateAndStoreOtp(String email, String type) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        String key = OTP_PREFIX + type.toLowerCase() + ":" + email;

        return redisTemplate.opsForValue()
                .set(key, otp, OTP_TTL)
                .doOnSuccess(r -> System.out.println("OTP stored in Redis: " + key))
                .thenReturn(otp);
    }

    // Verify OTP
    public Mono<Boolean> verifyOtp(String email, String type, String inputOtp) {
        String key = OTP_PREFIX + type.toLowerCase() + ":" + email;

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(storedOtp -> {
                    if (storedOtp.equals(inputOtp)) {
                        // Delete OTP after successful verification
                        return redisTemplate.delete(key)
                                .thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                })
                .defaultIfEmpty(false); // OTP expired or not found
    }

    // Check if OTP exists (for resend logic)
    public Mono<Boolean> otpExists(String email, String type) {
        String key = OTP_PREFIX + type.toLowerCase() + ":" + email;
        return redisTemplate.hasKey(key);
    }

    // Delete OTP manually
    public Mono<Void> deleteOtp(String email, String type) {
        String key = OTP_PREFIX + type.toLowerCase() + ":" + email;
        return redisTemplate.delete(key).then();
    }
}