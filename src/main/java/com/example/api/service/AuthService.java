package com.example.api.service;

import com.example.api.dto.request.*;
import com.example.api.dto.response.AuthResponse;
import com.example.api.dto.response.UserSummaryResponse;

public interface AuthService {
    UserSummaryResponse signup(SignupRequest req);
    AuthResponse login(LoginRequest req);
    void logout(LogoutRequest req);
    AuthResponse refresh(RefreshRequest req);
}