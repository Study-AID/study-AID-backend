package com.example.api.service;

import java.util.UUID;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.exception.auth.*;
import com.example.api.repository.RefreshTokenRepository;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSummaryResponse signupWithEmail(EmailSignupRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
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
                .orElseThrow(WrongLoginInputException::new);

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new WrongLoginInputException();
        }

        if (user.getAuthType() != AuthType.email) {
            throw new WrongAuthTypeException();
        }

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(
                new TokenResponse(accessToken, refreshToken),
                UserSummaryResponse.from(user)
        );
    }

    @Override
    public void logout(LogoutRequest req) {
        UUID userId = jwtProvider.extractUserId(req.getRefreshToken());
        refreshTokenRepository.deleteRefreshToken(userId);
    }

    @Override
    public AuthResponse refreshToken(TokenRefreshRequest req) {
        String refreshToken = req.getRefreshToken();

        if (!jwtProvider.isValid(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        UUID userId = jwtProvider.extractUserId(refreshToken);
        String storedRefreshToken = refreshTokenRepository.getRefreshToken(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RefreshTokenMismatchException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = jwtProvider.createAccessToken(user.getId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.saveRefreshToken(userId, newRefreshToken);

        return new AuthResponse(
                new TokenResponse(newAccessToken, newRefreshToken),
                UserSummaryResponse.from(user)
        );
    }

    @Override
    public UserSummaryResponse getCurrentUserInfo(String userIdString) {
        if (userIdString == null) {
            throw new InvalidAccessTokenException();
        }

        UUID userId = UUID.fromString(userIdString);
        User currentUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return UserSummaryResponse.from(currentUser);
    }
}
