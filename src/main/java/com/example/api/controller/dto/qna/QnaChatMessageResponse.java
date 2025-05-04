package com.example.api.controller.dto.qna;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class QnaChatMessageResponse {
    private String question;
    private String answer;
    private List<ChatMessage> messageHistory;
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    private List<String> recommendedQuestions;
}
