package com.example.api.controller.dto.qna;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class QnaChatMessageResponse {
    private String role;
    private String content;
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    private List<String> recommendedQuestions;
}
