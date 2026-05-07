package com.order.service.controllers;

import com.order.service.dto.AuthRequest;
import com.order.service.dto.AuthResponse;
import com.order.service.dto.OtpVerifyRequest;
import com.order.service.services.AuthService;
import com.order.service.services.UserCacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserCacheService userCacheService;

    public AuthController(AuthService authService, UserCacheService userCacheService) {
        this.authService = authService;
        this.userCacheService = userCacheService;
    }

    //Signup
    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, String>>> signUp(
            @RequestBody AuthRequest request) {
        return authService.signUp(
                        request.getEmail(),
                        request.getPassword(),
                        request.getCountryCode(),
                        request.getMobileNumber()
                )
                .map(msg -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", msg)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(Map.of("message", e.getMessage()))
                ));
    }

    //Verify Signup OTP
    @PostMapping("/verify-signup")
    public Mono<ResponseEntity<Map<String, String>>> verifySignup(
            @RequestBody OtpVerifyRequest request) {
        return authService.verifySignupOtp(request.getEmail(), request.getOtp())
                .map(msg -> ResponseEntity.ok(Map.of("message", msg)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(Map.of("message", e.getMessage()))
                ));
    }

    //Login
    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody AuthRequest request) {
        return authService.login(request.getEmail(), request.getPassword())
                .flatMap(result -> {
                    if (result.startsWith("LOGIN_SUCCESS:")) {
                        // OTP is OFF - return user data directly
                        String email = result.replace("LOGIN_SUCCESS:", "");
                        return userCacheService.findByEmail(email)
                                .map(user -> ResponseEntity.ok()
                                        .body((Object) new AuthResponse(
                                                user.getId(),
                                                user.getEmail(),
                                                user.getRole(),
                                                "Login successful",
                                                user.getCountryCode(),
                                                user.getMobileNumber()
                                        ))
                                );
                    } else {
                        // OTP is ON - return message to check email
                        return Mono.just(ResponseEntity.ok()
                                .body((Object) Map.of("message", result, "otpRequired", true)));
                    }
                })
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", e.getMessage()))
                ));
    }

    //Verify Login OTP
    @PostMapping("/verify-login")
    public Mono<ResponseEntity<Object>> verifyLogin(
            @RequestBody OtpVerifyRequest request) {
        return authService.verifyLoginOtp(request.getEmail(), request.getOtp())
                .map(user -> ResponseEntity.ok()
                        .body((Object) new AuthResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getRole(),
                                "Login successful",
                                user.getCountryCode(),
                                user.getMobileNumber()
                        ))
                )
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", e.getMessage()))
                ));
    }

    // Resend OTP
    @PostMapping("/resend-otp")
    public Mono<ResponseEntity<Map<String, String>>> resendOtp(
            @RequestBody OtpVerifyRequest request) {
        return authService.resendOtp(request.getEmail(), request.getType())
                .map(msg -> ResponseEntity.ok(Map.of("message", msg)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest()
                                .body(Map.of("message", e.getMessage()))
                ));
    }
}