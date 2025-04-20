package com.example.api.service;

import java.util.UUID;
import java.time.Duration;

import com.example.api.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

    private final JwtConfig jwtConfig;
    private static final String PREFIX = "userUuid:";

    @Override
    public void saveRefreshToken(UUID userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                PREFIX + userId.toString(),
                refreshToken,
                Duration.ofMillis(jwtConfig.getRefreshTokenTtlMs())
        );
    }

    @Override
    public String getRefreshToken(UUID userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId.toString());
    }

    @Override
    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(PREFIX + userId.toString());
    }
}