package com.example.api.service.dto.qna;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QnaChatMessageOutput {
    private String question;
    private String answer;
    private List<ChatMessage> messageHistory;
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    private List<String> recommendedQuestions;
}
