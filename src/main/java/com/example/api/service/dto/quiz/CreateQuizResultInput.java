package com.example.api.service.dto.quiz;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateQuizResultInput {
    private UUID quizId;
    private UUID userId;
    private Float score;
    private Float maxScore;
}
