package com.example.api.controller.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadQnaChatResponse {
    private UUID chatId;
    private List<MessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        private String role;
        private String content;
    }
}
