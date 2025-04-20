package com.example.api.jwt;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.api.entity.User;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    @Override
    public String createAccessToken(User user) {
        return jwtUtils.createToken(user.getId(), jwtProperties.getAccessTokenValidity());
    }

    @Override
    public String createRefreshToken(User user) {
        return jwtUtils.createToken(user.getId(), jwtProperties.getRefreshTokenValidity());
    }

    @Override
    public UUID extractUserId(String token) {
        return jwtUtils.getUserIdFromToken(token);
    }

    @Override
    public boolean isValid(String token) {
        return jwtUtils.validateToken(token);
    }
}