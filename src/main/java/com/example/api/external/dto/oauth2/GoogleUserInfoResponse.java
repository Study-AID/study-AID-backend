package com.example.api.external.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleUserInfoResponse {
    // Google 표준 OAuth2 UserInfo Response 필드명 (v2)
    private String id;
    private String email;

    @JsonProperty("verified_email")
    private Boolean verifiedEmail;

    private String name; // full name
    @JsonProperty("given_name")
    private String givenName; // First name
    @JsonProperty("family_name")
    private String familyName; // Last name

    private String picture;
    private String locale;
}