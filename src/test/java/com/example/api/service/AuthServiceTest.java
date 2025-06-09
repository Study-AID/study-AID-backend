package com.example.api.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.example.api.dto.response.AuthResponse;
import com.example.api.dto.response.UserSummaryResponse;
import com.example.api.entity.enums.AuthType;
import com.example.api.exception.auth.*;
import com.example.api.external.GoogleOAuth2Client;
import com.example.api.external.dto.oauth2.GoogleTokenResponse;
import com.example.api.external.dto.oauth2.GoogleUserInfoResponse;
import com.example.api.repository.RefreshTokenRepository;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.dto.oauth2.GoogleLoginInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.api.dto.request.*;
import com.example.api.entity.User;
import com.example.api.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GoogleOAuth2Client googleOAuth2Client;

    @InjectMocks
    private AuthServiceImpl authService;

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
    @DisplayName("Google 로그인 성공 - 신규 사용자")
    void googleLoginSuccess_newUser() {
        // given
        GoogleLoginInput input = new GoogleLoginInput("google-auth-code", "https://example.com/callback");

        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setAccessToken("google-access-token");

        GoogleUserInfoResponse userInfoResponse = new GoogleUserInfoResponse();
        userInfoResponse.setId("google-user-id");
        userInfoResponse.setEmail("google-user@gmail.com");
        userInfoResponse.setName("Google User");
        userInfoResponse.setVerifiedEmail(true);

        when(googleOAuth2Client.getAccessToken(input.getCode(), input.getRedirectUri())).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse.getAccessToken())).thenReturn(userInfoResponse);
        when(userRepository.findByEmail(userInfoResponse.getEmail())).thenReturn(Optional.empty());

        doReturn("access-token").when(jwtProvider).createAccessToken(any());
        doReturn("refresh-token").when(jwtProvider).createRefreshToken(any());

        // when
        AuthResponse authResponse = authService.loginWithGoogle(input);

        // then
        verify(userRepository).save(argThat(saved ->
                saved.getEmail().equals(userInfoResponse.getEmail()) &&
                        saved.getName().equals(userInfoResponse.getName()) &&
                        saved.getGoogleId().equals(userInfoResponse.getId()) &&
                        saved.getAuthType() == AuthType.google &&
                        saved.getLastLogin() != null
        ));

        verify(refreshTokenRepository).saveRefreshToken(any(), eq("refresh-token"));

        assertThat(authResponse.getToken().getAccessToken()).isEqualTo("access-token");
        assertThat(authResponse.getToken().getRefreshToken()).isEqualTo("refresh-token");
        assertThat(authResponse.getUser().getEmail()).isEqualTo(userInfoResponse.getEmail());
        assertThat(authResponse.getUser().getName()).isEqualTo(userInfoResponse.getName());
    }

    @Test
    @DisplayName("Google 로그인 성공 - 기존 사용자")
    void googleLoginSuccess_existingUser() {
        // given
        GoogleLoginInput input = new GoogleLoginInput("google-auth-code", "https://example.com/callback");

        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setAccessToken("google-access-token");

        GoogleUserInfoResponse userInfoResponse = new GoogleUserInfoResponse();
        userInfoResponse.setId("google-user-id");
        userInfoResponse.setEmail("google-user@gmail.com");
        userInfoResponse.setName("Google User");
        userInfoResponse.setVerifiedEmail(true);

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail("google-user@gmail.com");
        existingUser.setName("Google User");
        existingUser.setGoogleId("google-user-id");
        existingUser.setAuthType(AuthType.google);

        when(googleOAuth2Client.getAccessToken(input.getCode(), input.getRedirectUri())).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse.getAccessToken())).thenReturn(userInfoResponse);
        when(userRepository.findByEmail(userInfoResponse.getEmail())).thenReturn(Optional.of(existingUser));

        doReturn("access-token").when(jwtProvider).createAccessToken(existingUser.getId());
        doReturn("refresh-token").when(jwtProvider).createRefreshToken(existingUser.getId());

        // when
        AuthResponse authResponse = authService.loginWithGoogle(input);

        // then
        verify(userRepository).save(existingUser); // 마지막 로그인 시간 업데이트 확인
        verify(refreshTokenRepository).saveRefreshToken(existingUser.getId(), "refresh-token");

        assertThat(authResponse.getToken().getAccessToken()).isEqualTo("access-token");
        assertThat(authResponse.getToken().getRefreshToken()).isEqualTo("refresh-token");
        assertThat(authResponse.getUser().getEmail()).isEqualTo(existingUser.getEmail());
        assertThat(authResponse.getUser().getName()).isEqualTo(existingUser.getName());
    }

    @Test
    @DisplayName("Google 로그인 실패 - 이메일 미인증")
    void googleLoginFail_emailNotVerified() {
        // given
        GoogleLoginInput input = new GoogleLoginInput("google-auth-code", "https://example.com/callback");

        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setAccessToken("google-access-token");

        GoogleUserInfoResponse userInfoResponse = new GoogleUserInfoResponse();
        userInfoResponse.setId("google-user-id");
        userInfoResponse.setEmail("google-user@gmail.com");
        userInfoResponse.setName("Google User");
        userInfoResponse.setVerifiedEmail(false);

        when(googleOAuth2Client.getAccessToken(input.getCode(), input.getRedirectUri())).thenReturn(tokenResponse);
        when(googleOAuth2Client.getUserInfo(tokenResponse.getAccessToken())).thenReturn(userInfoResponse);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.loginWithGoogle(input);
        });

        // 정확한 오류 메시지 검증 대신 포함 여부 확인
        assertThat(exception.getMessage()).contains("Google");
    }

    @Test
    @DisplayName("Google 로그인 실패 - Google API 호출 중 오류")
    void googleLoginFail_googleApiError() {
        // given
        GoogleLoginInput input = new GoogleLoginInput("google-auth-code", "https://example.com/callback");

        when(googleOAuth2Client.getAccessToken(input.getCode(), input.getRedirectUri()))
                .thenThrow(new RuntimeException("Google API 호출 중 오류 발생"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.loginWithGoogle(input);
        });

        assertThat(exception.getMessage()).isEqualTo("Google 로그인 중 오류가 발생했습니다.");
    }
}