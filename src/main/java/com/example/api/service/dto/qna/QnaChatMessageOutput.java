package com.example.api.service.dto.qna;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QnaChatMessageOutput {
    private UUID messageId;
    private String role;
    private String content;
    private List<ChatMessage> messageContext;
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    private List<String> recommendedQuestions;
    private LocalDateTime createdAt;
    private boolean isLiked;
}
