package com.example.api.controller.dto.qna;

import com.example.api.external.dto.langchain.ReferenceResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class QnaChatMessageResponse {
    @NotNull
    private UUID messageId;
    @NotNull
    private String role;
    @NotNull
    private String content;
    @NotNull
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    @NotNull
    private List<String> recommendedQuestions;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    @JsonProperty("isLiked")
    private boolean isLiked;
}
