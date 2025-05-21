package com.example.api.controller.dto.qna;

import com.example.api.external.dto.langchain.ReferenceResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class QnaChatMessageResponse {
    @NotNull
    private String role;
    @NotNull
    private String content;
    @NotNull
    private List<ReferenceResponse.ReferenceChunkResponse> references;
    @NotNull
    private List<String> recommendedQuestions;
}
