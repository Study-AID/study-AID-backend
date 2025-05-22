package com.example.api.controller;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.exception.auth.*;
import com.example.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("이메일 회원가입 성공 테스트")
    void emailSignupTest() throws Exception {
        // given
        EmailSignupRequest request = new EmailSignupRequest("test@example.com", "password1234", "테스트");
        UserSummaryResponse response = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트"
        );

        when(authService.signupWithEmail(any(EmailSignupRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 회원가입 성공"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트"));

        verify(authService, times(1)).signupWithEmail(any(EmailSignupRequest.class));
    }

    @Test
    @DisplayName("이메일 로그인 성공 테스트")
    void emailLoginTest() throws Exception {
        // given
        EmailLoginRequest request = new EmailLoginRequest("test@example.com", "password1234");
        TokenResponse tokenResponse = new TokenResponse("sample-access-token", "sample-refresh-token");
        UserSummaryResponse userResponse = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트"
        );
        AuthResponse authResponse = new AuthResponse(tokenResponse, userResponse);

        when(authService.loginWithEmail(any(EmailLoginRequest.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 로그인 성공"))
                .andExpect(jsonPath("$.data.token.accessToken").value("sample-access-token"))
                .andExpect(jsonPath("$.data.token.refreshToken").value("sample-refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.name").value("테스트"));

        verify(authService, times(1)).loginWithEmail(any(EmailLoginRequest.class));
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutTest() throws Exception {
        // given
        LogoutRequest request = new LogoutRequest("sample-refresh-token");

        doNothing().when(authService).logout(any(LogoutRequest.class));

        // when & then
        mockMvc.perform(post("/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService, times(1)).logout(any(LogoutRequest.class));
    }

    @Test
    @DisplayName("토큰 재발급 성공 테스트")
    void tokenRefreshTest() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest("sample-refresh-token");
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token");
        UserSummaryResponse userResponse = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트"
        );
        AuthResponse authResponse = new AuthResponse(tokenResponse, userResponse);

        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰 재발급 성공"))
                .andExpect(jsonPath("$.data.token.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.token.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.name").value("테스트"));

        verify(authService, times(1)).refreshToken(any(TokenRefreshRequest.class));
    }

    /* TODO(jin): use authorized user
    @Test
    @DisplayName("사용자 정보 조회 성공 테스트")
    void getCurrentUserInfoTest() throws Exception {
        // given
        UserSummaryResponse response = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트"
        );

        when(authService.getCurrentUserInfo(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/v1/auth/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 정보 조회 성공"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트"));

        verify(authService, times(1)).getCurrentUserInfo(any());
    }*/

    @Test
    @DisplayName("이메일 회원가입 실패 - 이미 가입된 이메일")
    void signupFail_emailExists() throws Exception {
        // given
        EmailSignupRequest request = new EmailSignupRequest("test@example.com", "password", "테스트");
        doThrow(new EmailAlreadyExistsException()).when(authService).signupWithEmail(any());

        // when & then
        mockMvc.perform(post("/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 로그인 실패 - 이메일/비밀번호 불일치")
    void loginFail_wrongInput() throws Exception {
        // given
        EmailLoginRequest request = new EmailLoginRequest("wrong@example.com", "wrong");
        doThrow(new WrongLoginInputException()).when(authService).loginWithEmail(any());

        // when & then
        mockMvc.perform(post("/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("이메일 로그인 실패 - 이메일 로그인 사용자 아님")
    void loginFail_wrongAuthType() throws Exception {
        // given
        EmailLoginRequest request = new EmailLoginRequest("social@example.com", "pw");
        doThrow(new WrongAuthTypeException())
                .when(authService).loginWithEmail(any());

        // when & then
        mockMvc.perform(post("/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 로그인 사용자가 아닙니다. 다른 로그인 방식으로 시도해보세요."));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void tokenRefreshFail_invalidToken() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest("invalid-token");
        doThrow(new InvalidRefreshTokenException()).when(authService).refreshToken(any());

        // when & then
        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 불일치")
    void tokenRefreshFail_mismatch() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest("wrong-token");
        doThrow(new RefreshTokenMismatchException()).when(authService).refreshToken(any());

        // when & then
        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 사용자 없음")
    void tokenRefreshFail_userNotFound() throws Exception {
        // given
        TokenRefreshRequest request = new TokenRefreshRequest("valid-but-user-missing");
        doThrow(new UserNotFoundException()).when(authService).refreshToken(any());

        // when & then
        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    /* TODO(jin): use authorized user
    @Test
    @DisplayName("내 정보 조회 실패 - 유효하지 않은 액세스 토큰")
    void meFail_invalidToken() throws Exception {
        // given
        doThrow(new InvalidAccessTokenException()).when(authService).getCurrentUserInfo(any());

        // when & then
        mockMvc.perform(get("/v1/auth/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 액세스 토큰입니다."));
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 사용자 없음")
    void meFail_userNotFound() throws Exception {
        // given
        doThrow(new UserNotFoundException()).when(authService).getCurrentUserInfo(any());

        // when & then
        mockMvc.perform(get("/v1/auth/me"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }*/
}
