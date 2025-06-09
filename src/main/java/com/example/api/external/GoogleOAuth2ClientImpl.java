package com.example.api.external;

import com.example.api.config.GoogleOAuth2Config;
import com.example.api.external.dto.oauth2.GoogleTokenResponse;
import com.example.api.external.dto.oauth2.GoogleUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2ClientImpl implements GoogleOAuth2Client {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2ClientImpl.class);

    private final GoogleOAuth2Config googleOAuth2Config;
    private final RestTemplate restTemplate;

    /**
     * Authorization code를 사용하여 Google에서 Access Token을 얻습니다.
     */
    @Override
    public GoogleTokenResponse getAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // form data를 통해 google에 요청

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleOAuth2Config.getClientId());
        params.add("client_secret", googleOAuth2Config.getClientSecret());
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers); // 따라서 JSON dto 대신 map 형태로 요청

        try {
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    googleOAuth2Config.getTokenUri(),
                    request,
                    GoogleTokenResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Failed to get access token from Google. Status: {}", response.getStatusCode());
                throw new RuntimeException("Google 액세스 토큰 발급에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("Error while getting access token from Google", e);
            throw new RuntimeException("Google 액세스 토큰 발급 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Access Token을 사용하여 Google에서 사용자 정보를 얻는다
     */
    @Override
    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                    googleOAuth2Config.getUserInfoUri(),
                    HttpMethod.GET,
                    request,
                    GoogleUserInfoResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Failed to get user info from Google. Status: {}", response.getStatusCode());
                throw new RuntimeException("Google 사용자 정보 조회에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("Error while getting user info from Google", e);
            throw new RuntimeException("Google 사용자 정보 조회 중 오류가 발생했습니다.", e);
        }
    }
}