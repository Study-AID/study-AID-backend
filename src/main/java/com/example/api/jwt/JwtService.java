package com.example.api.jwt;

import java.util.UUID;
import com.example.api.entity.User;

public interface JwtService {
    String createAccessToken(User user);
    String createRefreshToken(User user);
    UUID extractUserId(String token);
    boolean isValid(String token);
}
