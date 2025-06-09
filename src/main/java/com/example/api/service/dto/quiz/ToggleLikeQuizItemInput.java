package com.example.api.service.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ToggleLikeQuizItemInput {
    private UUID quizId;
    private UUID quizItemId;
    private UUID userId;
}