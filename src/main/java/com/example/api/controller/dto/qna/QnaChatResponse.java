package com.example.api.controller.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class QnaChatResponse {
    private String question;
    private String answer;
    private String source;
    private List<String> recommendedQuestions;
}
