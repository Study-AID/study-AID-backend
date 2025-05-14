package com.example.api.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    @NotNull
    private TokenResponse token;

    @NotNull
    private UserSummaryResponse user;
}
