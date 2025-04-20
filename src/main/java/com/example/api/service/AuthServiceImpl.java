package com.example.api.service;

import java.util.UUID;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSummaryResponse signupWithEmail(EmailSignupRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setAuthType(AuthType.email);

        userRepository.save(user);
        return UserSummaryResponse.from(user);
    }

    @Override
    public AuthResponse loginWithEmail(EmailLoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (user.getAuthType() != AuthType.email) {
            throw new RuntimeException("이메일 로그인 사용자가 아닙니다. 다른 로그인 방식으로 시도해보세요.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        redisService.saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(
                new TokenResponse(accessToken, refreshToken),
                UserSummaryResponse.from(user)
        );
    }

    @Override
    public void logout(LogoutRequest req) {
        UUID userId = jwtProvider.extractUserId(req.getRefreshToken());
        redisService.deleteRefreshToken(userId);
    }

    @Override
    public AuthResponse tokenRefresh(TokenRefreshRequest req) {
        String refreshToken = req.getRefreshToken();

        if (!jwtProvider.isValid(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        UUID userId = jwtProvider.extractUserId(refreshToken);
        String storedRefreshToken = redisService.getRefreshToken(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        redisService.saveRefreshToken(userId, newRefreshToken);

        return new AuthResponse(
                new TokenResponse(newAccessToken, newRefreshToken),
                UserSummaryResponse.from(user)
        );
    }
}
