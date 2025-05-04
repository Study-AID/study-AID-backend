package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReadQnaChatOutput {
    private List<MessageItem> messageItems;

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        private String question;
        private String answer;
    }
}
