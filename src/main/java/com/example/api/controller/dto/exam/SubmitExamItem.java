package com.example.api.controller.dto.exam;

import java.util.UUID;

import com.example.api.entity.enums.QuestionType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmitExamItem {
    private UUID quizItemId;

    private QuestionType questionType;

    private Boolean selectedBool;

    private Integer[] selectedIndices;

    private String textAnswer;
}
