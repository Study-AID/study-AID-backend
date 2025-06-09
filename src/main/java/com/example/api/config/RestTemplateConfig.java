package com.example.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

// 현재는 Langchain 서버, Google api(OAuth2) 와의 통신을 위해 RestTemplate을 사용하고 있습니다.
@Configuration
public class RestTemplateConfig {
    @Value("${rest-template.connect-timeout-ms:3000}")
    private int connectTimeoutMs; // 3초

    @Value("${rest-template.read-timeout-ms:60000}")
    private int readTimeoutMs; // 60초

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
