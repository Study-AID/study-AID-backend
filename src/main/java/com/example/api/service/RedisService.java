package com.example.api.service;

import java.util.UUID;

public interface RedisService {
    void saveRefreshToken(UUID userId, String refreshToken);
    String getRefreshToken(UUID userId);
    void deleteRefreshToken(UUID userId);
}