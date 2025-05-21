package com.example.api.controller.dto.quiz;

import java.util.UUID;

import com.example.api.entity.enums.QuestionType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmitQuizItem {
    private UUID quizItemId;

    private QuestionType questionType;

    private Boolean selectedBool;

    private Integer[] selectedIndices;

    private String textAnswer;
}
