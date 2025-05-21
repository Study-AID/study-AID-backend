package com.example.api.controller.dto.qna;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadQnaChatResponse {
    @NotNull
    private UUID chatId;
    @NotNull
    private List<MessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        @NotNull
        private String role;
        @NotNull
        private String content;
    }
}
