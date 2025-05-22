package com.example.api.security.jwt;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.api.config.JwtConfig;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtConfig jwtConfig;

    private Key getSigningKey() {
        String secretKey = jwtConfig.getSecretKey();
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(UUID userId) {
        return createToken(userId, jwtConfig.getAccessTokenTtlMs());
    }

    public String createRefreshToken(UUID userId) {
        return createToken(userId, jwtConfig.getRefreshTokenTtlMs());
    }

    private String createToken(UUID userId, long ttlMs) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(jwtConfig.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
