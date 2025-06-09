package com.example.api.service.dto.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GoogleLoginInput {
    private String code; // Google에서 받은 authorization code
    private String redirectUri; // 프론트엔드에서 사용한 redirect URI
}