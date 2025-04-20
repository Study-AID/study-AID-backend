package com.example.api.controller;

import com.example.api.dto.request.*;
import com.example.api.dto.response.*;
import com.example.api.entity.enums.AuthType;
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
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupTest() throws Exception {
        // given
        SignupRequest request = new SignupRequest("test@example.com", "password1234", "테스트");
        UserSummaryResponse response = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트",
                "hashedPassword",
                AuthType.email
        );

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트"));

        verify(authService, times(1)).signup(any(SignupRequest.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginTest() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password1234");
        TokenResponse tokenResponse = new TokenResponse("sample-access-token", "sample-refresh-token");
        UserSummaryResponse userResponse = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트",
                "hashedPassword",
                AuthType.email
        );
        AuthResponse response = new AuthResponse(tokenResponse, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.token.accessToken").value("sample-access-token"))
                .andExpect(jsonPath("$.data.token.refreshToken").value("sample-refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.name").value("테스트"));

        verify(authService, times(1)).login(any(LoginRequest.class));
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
    void refreshTest() throws Exception {
        // given
        RefreshRequest request = new RefreshRequest("sample-refresh-token");
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token");
        UserSummaryResponse userResponse = new UserSummaryResponse(
                userId,
                "test@example.com",
                "테스트",
                "hashedPassword",
                AuthType.email
        );
        AuthResponse response = new AuthResponse(tokenResponse, userResponse);

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

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

        verify(authService, times(1)).refresh(any(RefreshRequest.class));
    }
}
