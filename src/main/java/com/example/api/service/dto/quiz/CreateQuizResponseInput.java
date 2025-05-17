package com.example.api.service.dto.quiz;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateQuizResponseInput {
    private UUID quizId;

    private UUID quizItemId;

    private UUID userId;

    private Boolean selectedBool;

    private Integer[] selectedIndices;

    private String textAnswer;
}
