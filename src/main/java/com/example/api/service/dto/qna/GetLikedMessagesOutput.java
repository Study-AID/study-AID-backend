package com.example.api.service.dto.qna;

import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetLikedMessagesOutput {
    private UUID chatId;
    private List<LikedMessageItem> messages;

    @Getter
    @AllArgsConstructor
    public static class LikedMessageItem {
        private UUID messageId;
        private String role;
        private String content;
        private List<ReferenceResponse.ReferenceChunkResponse> references;
        private LocalDateTime createdAt;
        private boolean isLiked;
    }
}