package com.example.api.external.dto.langchain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageHistoryRequest {
    private UUID chatId;
    private UUID lectureId;
    private String question;
    private List<MessageHistoryItem> messageHistoryItems;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageHistoryItem {
        private String question;
        private String answer;
    }
}
