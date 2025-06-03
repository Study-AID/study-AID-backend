package com.example.api.controller.dto.qna;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetQnaChatMessagesResponse {
    @NotNull
    private UUID chatId;
    @NotNull
    private List<MessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        @NotNull
        private UUID messageId;
        @NotNull
        private String role;
        @NotNull
        private String content;
        @NotNull
        private LocalDateTime createdAt;
        @NotNull
        private boolean isLiked;
    }
}
