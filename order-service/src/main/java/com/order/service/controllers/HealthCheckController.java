package com.order.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    @GetMapping("/health")
    public String healthCheck(){
        logger.info("The /health endpoint was called successfully!");
        return "Hey there! I'm up.";
    }
}
