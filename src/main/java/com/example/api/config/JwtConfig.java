package com.example.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties("jwt")
public class JwtConfig {
    private String issuer;
    private String secretKey;
    private long accessTokenTtlMs;
    private long refreshTokenTtlMs;
}
