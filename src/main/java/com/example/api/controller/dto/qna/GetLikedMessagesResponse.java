package com.example.api.controller.dto.qna;

import com.example.api.external.dto.langchain.ReferenceResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"liked"}) // getter 메소드 isLiked()를 보고 Jackson이 JSON에 자동 생성하는 liked 필드는 무시
    public static class LikedMessageItem {
        @NotNull
        private UUID messageId;
        @NotNull
        private String role;
        @NotNull
        private String content;
        private List<ReferenceResponse.ReferenceChunkResponse> references;
        @NotNull
        private LocalDateTime createdAt;
        @NotNull
        @JsonProperty("isLiked") // 해당 필드를 JSON 응답에 isLiked로 표시하도록 명시
        private boolean isLiked;
    }
}
