package com.example.api.controller.dto.qna;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetLikedMessagesResponse {
    @NotNull
    private UUID chatId;
    @NotNull
    private List<LikedMessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class LikedMessageItem {
        @NotNull
        private UUID messageId;
        @NotNull
        private String role;
        @NotNull
        private String content;
        @NotNull
        private LocalDateTime createdAt;
        @NotNull
        @JsonProperty("isLiked")
        private boolean isLiked;
    }
}
