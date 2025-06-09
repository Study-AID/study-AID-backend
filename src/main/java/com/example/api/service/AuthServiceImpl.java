package com.example.api.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.exception.auth.*;
import com.example.api.external.GoogleOAuth2Client;
import com.example.api.external.dto.oauth2.GoogleTokenResponse;
import com.example.api.external.dto.oauth2.GoogleUserInfoResponse;
import com.example.api.repository.RefreshTokenRepository;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.dto.oauth2.GoogleLoginInput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleOAuth2Client googleOAuth2Client;

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
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginInput input) {
        try {
            // 1. Google에서 Access Token 획득
            GoogleTokenResponse tokenResponse = googleOAuth2Client.getAccessToken(
                    input.getCode(),
                    input.getRedirectUri()
            );

            // 2. Access Token으로 사용자 정보 획득
            GoogleUserInfoResponse googleUserInfoResponse = googleOAuth2Client.getUserInfo(
                    tokenResponse.getAccessToken()
            );

            // 3. 이메일 인증 여부 확인
            if (googleUserInfoResponse.getVerifiedEmail() == null || !googleUserInfoResponse.getVerifiedEmail()) {
                throw new RuntimeException("Google 계정의 이메일이 인증되지 않았습니다.");
            }

            // 4. 기존 사용자 확인 또는 새 사용자 생성
            Optional<User> existingUser = userRepository.findByEmail(googleUserInfoResponse.getEmail());
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();

                // 기존 사용자가 다른 인증 방식으로 가입된 경우
                if (user.getAuthType() != AuthType.google) {
                    throw new WrongAuthTypeException();
                }

                // google user info 업데이트 (변경된 경우)
                if (!googleUserInfoResponse.getId().equals(user.getGoogleId())) {
                    user.setGoogleId(googleUserInfoResponse.getId());
                }

                // 마지막 로그인 시간 업데이트
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

            } else {
                // 우리 서비스용 새 사용자 객체 생성
                user = new User();
                user.setEmail(googleUserInfoResponse.getEmail());
                user.setName(googleUserInfoResponse.getName());
                user.setGoogleId(googleUserInfoResponse.getId());
                user.setAuthType(AuthType.google);
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            }

            // 5. JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(user.getId());
            String refreshToken = jwtProvider.createRefreshToken(user.getId());
            refreshTokenRepository.saveRefreshToken(user.getId(), refreshToken);

            return new AuthResponse(
                    new TokenResponse(accessToken, refreshToken),
                    UserSummaryResponse.from(user)
            );

        } catch (WrongAuthTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Google 로그인 중 오류가 발생했습니다.", e);
        }
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
