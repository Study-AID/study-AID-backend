package com.example.api.service.dto.qna;

import com.example.api.external.dto.langchain.ReferenceResponse;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
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
    private boolean hasMore;
    private UUID nextCursor;

    // Lombok @Getter 실행 시 boolean은 isLiked 처럼 is가 없다면, is Prefix가 붙어 isHasMore로 getter 메서드가 생성되므로 getter 수동 생성
    public boolean getHasMore() {
        return hasMore;
    }

    @Getter
    @AllArgsConstructor
    public static class MessageItem {
        private UUID messageId;
        private String role;
        private String content;
        @Nullable
        private List<ReferenceResponse.ReferenceChunkResponse> references;
        private LocalDateTime createdAt;
        private boolean isLiked;
    }
}
