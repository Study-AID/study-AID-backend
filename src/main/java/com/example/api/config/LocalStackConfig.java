package com.example.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "localstack")
@Getter
@Setter
public class LocalStackConfig {
    private String endpoint;
}
