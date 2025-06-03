package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetQnaChatMessagesOutput {
    private UUID chatId;
    private List<MessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        private UUID messageId;
        private String role;
        private String content;
        private LocalDateTime createdAt;
        private boolean isLiked;
    }
}
