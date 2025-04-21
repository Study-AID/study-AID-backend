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

    private static final String USER_REFRESH_TOKEN_KEY_FORMAT = "user:<%s>";

    private String buildUserRefTokenKey(UUID userId) {
        return String.format(USER_REFRESH_TOKEN_KEY_FORMAT, userId);
    }
    @Override
    public void saveRefreshToken(UUID userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                buildUserRefTokenKey(userId),
                refreshToken,
                Duration.ofMillis(jwtConfig.getRefreshTokenTtlMs())
        );
    }

    @Override
    public String getRefreshToken(UUID userId) {
        return redisTemplate.opsForValue().get(buildUserRefTokenKey(userId));
    }

    @Override
    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(buildUserRefTokenKey(userId));
    }
}