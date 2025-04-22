package com.example.api.repository;

import java.util.UUID;

public interface RefreshTokenRepository {
    void saveRefreshToken(UUID userId, String refreshToken);
    String getRefreshToken(UUID userId);
    void deleteRefreshToken(UUID userId);
}