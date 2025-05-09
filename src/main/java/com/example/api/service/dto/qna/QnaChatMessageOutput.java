package com.example.api.service.dto.qna;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QnaChatMessageOutput {
    private String role;
    private String content;
    private List<ChatMessage> messageContext;
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    private List<String> recommendedQuestions;
}
