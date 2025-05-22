package com.example.api.service;

import java.util.Optional;
import java.util.UUID;

import com.example.api.dto.response.AuthResponse;
import com.example.api.dto.response.UserSummaryResponse;
import com.example.api.exception.auth.*;
import com.example.api.repository.RefreshTokenRepository;
import com.example.api.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.api.dto.request.*;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtProvider jwtProvider;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthServiceImpl authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setName("회원가입 한 유저");
        user.setAuthType(AuthType.email);
    }

    @Test
    @DisplayName("이메일 회원가입 성공")
    void emailSignupSuccess() {
        // given
        EmailSignupRequest req = new EmailSignupRequest("new@example.com", "newPassword", "회원가입 할 유저");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");

        // when
        UserSummaryResponse userSummaryResponse = authService.signupWithEmail(req);

        // then
        verify(userRepository).save(argThat(saved ->
                saved.getEmail().equals(req.getEmail()) &&
                        saved.getName().equals(req.getName()) &&
                        saved.getPasswordHash().equals("encoded") &&
                        saved.getAuthType() == AuthType.email
        ));
        assertThat(userSummaryResponse.getEmail()).isEqualTo(req.getEmail());
        assertThat(userSummaryResponse.getName()).isEqualTo(req.getName());
    }

    @Test
    @DisplayName("이메일 회원가입 실패 - 이미 가입된 이메일")
    void emailSignupFail_emailAlreadyExists() {
        // given
        EmailSignupRequest req = new EmailSignupRequest("new@example.com", "newPassword", "회원가입 하려는 유저");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));

        // when & then
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.signupWithEmail(req);
        });

        assertThat(exception.getMessage()).isEqualTo("이미 가입된 이메일입니다.");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 로그인 성공")
    void emailLoginSuccess() {
        // given
        EmailLoginRequest req = new EmailLoginRequest("test@example.com", "password123");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtProvider.createAccessToken(user.getId())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(user.getId())).thenReturn("refresh-token");

        // when
        AuthResponse authResponse = authService.loginWithEmail(req);

        // then
        assertThat(authResponse.getToken().getAccessToken()).isEqualTo("access-token");
        assertThat(authResponse.getToken().getRefreshToken()).isEqualTo("refresh-token");
        assertThat(authResponse.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(authResponse.getUser().getName()).isEqualTo(user.getName());
        verify(refreshTokenRepository).saveRefreshToken(user.getId(), "refresh-token");
    }

    @Test
    @DisplayName("이메일 로그인 실패 - 존재하지 않는 이메일")
    void emailLoginFail_emailNotFound() {
        // given
        EmailLoginRequest req = new EmailLoginRequest("nonexistent@example.com", "password");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        // when & then
        WrongLoginInputException exception = assertThrows(WrongLoginInputException.class, () -> {
            authService.loginWithEmail(req);
        });

        assertThat(exception.getMessage()).isEqualTo("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("이메일 로그인 실패 - 비밀번호 불일치")
    void emailLoginFail_wrongPassword() {
        // given
        EmailLoginRequest req = new EmailLoginRequest("test@example.com", "wrongPassword");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPasswordHash())).thenReturn(false);

        // when & then
        WrongLoginInputException exception = assertThrows(WrongLoginInputException.class, () -> {
            authService.loginWithEmail(req);
        });

        assertThat(exception.getMessage()).isEqualTo("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 로그인 사용자가 아님")
    void emailLoginFail_notEmailUser() {
        // given
        EmailLoginRequest req = new EmailLoginRequest("test@gmail.com", "password");
        User socialUser = new User();
        socialUser.setEmail("test@gmail.com");
        socialUser.setPasswordHash("hashedPassword");
        socialUser.setAuthType(AuthType.google);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(socialUser));
        when(passwordEncoder.matches(req.getPassword(), "hashedPassword")).thenReturn(true);

        // when & then
        WrongAuthTypeException exception = assertThrows(WrongAuthTypeException.class, () -> {
            authService.loginWithEmail(req);
        });

        assertThat(exception.getMessage()).isEqualTo("이메일 로그인 사용자가 아닙니다. 다른 로그인 방식으로 시도해보세요.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void tokenRefreshFail_invalidToken() {
        // given
        String invalidToken = "invalid-token";
        TokenRefreshRequest req = new TokenRefreshRequest(invalidToken);
        when(jwtProvider.isValid(invalidToken)).thenReturn(false);

        // when & then
        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class, () -> {
            authService.refreshToken(req);
        });

        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 저장된 리프레시 토큰과 불일치")
    void tokenRefreshFail_tokenMismatch() {
        // given
        String refreshToken = "refresh-token";
        TokenRefreshRequest req = new TokenRefreshRequest(refreshToken);
        when(jwtProvider.isValid(refreshToken)).thenReturn(true);
        when(jwtProvider.extractUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.getRefreshToken(userId)).thenReturn("different-token");

        // when & then
        RefreshTokenMismatchException exception = assertThrows(RefreshTokenMismatchException.class, () -> {
            authService.refreshToken(req);
        });

        assertThat(exception.getMessage()).isEqualTo("리프레시 토큰이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 사용자 없음")
    void tokenRefreshFail_userNotFound() {
        // given
        String refreshToken = "refresh-token";
        TokenRefreshRequest req = new TokenRefreshRequest(refreshToken);
        when(jwtProvider.isValid(refreshToken)).thenReturn(true);
        when(jwtProvider.extractUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenRepository.getRefreshToken(userId)).thenReturn(refreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authService.refreshToken(req);
        });

        assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 유효하지 않은 액세스 토큰")
    void getCurrentUserInfoFail_invalidToken() {
        // when & then
        InvalidAccessTokenException exception = assertThrows(InvalidAccessTokenException.class, () -> {
            authService.getCurrentUserInfo(null);
        });

        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 액세스 토큰입니다.");
    }


    @Test
    @DisplayName("사용자 정보 조회 실패 - 사용자 없음")
    void getCurrentUserInfoFail_userNotFound() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // when & then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authService.getCurrentUserInfo(user.getId().toString());
        });

        assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() {
        // given
        String refreshToken = "refresh-token";
        LogoutRequest req = new LogoutRequest(refreshToken);
        when(jwtProvider.extractUserId(refreshToken)).thenReturn(userId);

        // when
        authService.logout(req);

        // then
        verify(refreshTokenRepository).deleteRefreshToken(userId);
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getCurrentUserInfoSuccess() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // when
        UserSummaryResponse response = authService.getCurrentUserInfo(user.getId().toString());

        // then
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getName()).isEqualTo(user.getName());
    }
}
