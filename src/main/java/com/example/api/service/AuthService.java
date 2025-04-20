package com.example.api.service;

import com.example.api.dto.request.*;
import com.example.api.dto.response.AuthResponse;
import com.example.api.dto.response.UserSummaryResponse;

public interface AuthService {
    UserSummaryResponse signupWithEmail(EmailSignupRequest req);
    AuthResponse loginWithEmail(EmailLoginRequest req);
    void logout(LogoutRequest req);
    AuthResponse tokenRefresh(TokenRefreshRequest req);
}