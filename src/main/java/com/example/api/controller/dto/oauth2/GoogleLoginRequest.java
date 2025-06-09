package com.example.api.controller.dto.oauth2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    private String code; // Google에서 받은 authorization code
    private String redirectUri; // 프론트엔드에서 사용한 redirect URI
}
