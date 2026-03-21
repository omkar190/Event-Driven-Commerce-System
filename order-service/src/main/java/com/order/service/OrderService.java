package com.order.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderService {

    public static void main(String[] args) {
        SpringApplication.run(OrderService.class, args);
        System.out.println("--- Spring Boot Application Started Successfully! ---");
    }

}
