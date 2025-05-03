package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QnaChatOutput {
    private String question;
    private String answer;
    private String source;
    private List<String> recommendedQuestions;
}
