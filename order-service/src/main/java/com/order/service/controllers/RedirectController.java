package com.order.service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class RedirectController {

    @GetMapping("/")
    public Mono<Void> redirect(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(
                java.net.URI.create("https://order-ui-y50d.onrender.com")
        );
        return exchange.getResponse().setComplete();
    }
}