package com.example.api.controller.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class QnaChatMessageResponse {
    private String question;
    private String answer;
    private List<String> messageHistory;
    private List<String> recommendedQuestions;
}
