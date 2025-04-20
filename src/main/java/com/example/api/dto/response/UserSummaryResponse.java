package com.example.api.dto.response;

import java.util.UUID;

import com.example.api.entity.enums.AuthType;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.example.api.entity.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private UUID id;
    private String email;
    private String name;
    private String passwordHash;
    private AuthType authType;

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getEmail(), user.getName(), user.getPasswordHash(), user.getAuthType());
    }
}
