package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QnaChatMessageOutput {
    private String question;
    private String answer;
    private List<String> messageHistory;
    private List<String> recommendedQuestions;
}
