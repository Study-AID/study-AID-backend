package com.example.api.external.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenResponse {
    // Google 표준 OAuth2 UserInfo Response 필드명 (v2)
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn; // 액세스 토큰 만료 시간: 기본적으로 3600 seconds (1 hour)

    @JsonProperty("refresh_token")
    private String refreshToken; // 리프레시 토큰 만료 시간: 기본적으로 6개월

    @JsonProperty("scope")
    private String scope; // 부여된 권한

    @JsonProperty("token_type")
    private String tokenType; // Bearer

    @JsonProperty("id_token")
    private String idToken; // ID 토큰 (JWT 형식). 디코딩하여 바로 사용자 정보 추출 가능
}