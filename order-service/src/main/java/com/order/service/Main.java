package com.order.service;

import com.order.service.config.FeatureFlags;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(FeatureFlags.class)
public class Main {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(Main.class, args);
        System.out.println("--- Spring Boot Application Started Successfully! ---");
    }

}
