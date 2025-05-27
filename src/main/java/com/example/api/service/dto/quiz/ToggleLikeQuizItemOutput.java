package com.example.api.service.dto.quiz;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ToggleLikeQuizItemOutput {
    private UUID quizId;
    private UUID quizItemId;
    private UUID userId;
    private boolean isLiked;
    
    public ToggleLikeQuizItemOutput(UUID quizId, UUID quizItemId, UUID userId, boolean isLiked) {
        this.quizId = quizId;
        this.quizItemId = quizItemId;
        this.userId = userId;
        this.isLiked = isLiked;
    }
}