package com.example.api.external;

import com.example.api.external.dto.oauth2.GoogleTokenResponse;
import com.example.api.external.dto.oauth2.GoogleUserInfoResponse;

public interface GoogleOAuth2Client {
    GoogleTokenResponse getAccessToken(String code, String redirectUri);
    GoogleUserInfoResponse getUserInfo(String accessToken);
}
