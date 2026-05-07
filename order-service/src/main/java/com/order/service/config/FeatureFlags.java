package com.order.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.features")
@Getter
@Setter
public class FeatureFlags {

    private boolean otpEnabled;

}