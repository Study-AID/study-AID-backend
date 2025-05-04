package com.example.api.controller.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReadQnaChatResponse {
    private List<MessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        private String question;
        private String answer;
    }
}
