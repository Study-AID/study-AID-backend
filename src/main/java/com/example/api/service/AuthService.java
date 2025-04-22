package com.example.api.service;

import com.example.api.dto.request.*;
import com.example.api.dto.response.AuthResponse;
import com.example.api.dto.response.UserSummaryResponse;
import com.example.api.entity.User;

public interface AuthService {
    UserSummaryResponse signupWithEmail(EmailSignupRequest req);
    AuthResponse loginWithEmail(EmailLoginRequest req);
    void logout(LogoutRequest req);
    AuthResponse refreshToken(TokenRefreshRequest req);
    UserSummaryResponse getCurrentUserInfo(User user);
}