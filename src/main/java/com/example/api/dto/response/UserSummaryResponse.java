package com.example.api.dto.response;

import com.example.api.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    @NotNull
    private UUID id;

    @NotNull
    private String email;

    @NotNull
    private String name;

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getEmail(), user.getName());
    }
}
