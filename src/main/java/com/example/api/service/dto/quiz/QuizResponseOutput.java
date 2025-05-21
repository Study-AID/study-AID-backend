package com.example.api.service.dto.quiz;

import com.example.api.entity.QuizResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseOutput {
    private UUID id;
    private UUID quizId;
    private UUID quizItemId;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizResponseOutput fromEntity(QuizResponse quizResponse) {
        return new QuizResponseOutput(
                quizResponse.getId(),
                quizResponse.getQuiz().getId(),
                quizResponse.getQuizItem().getId(),
                quizResponse.getUser().getId(),
                quizResponse.getCreatedAt(),
                quizResponse.getUpdatedAt()
        );
    }
}
