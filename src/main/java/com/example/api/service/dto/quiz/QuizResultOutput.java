package com.example.api.service.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.api.entity.QuizResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultOutput {
    private UUID id;
    private UUID quizId;
    private UUID userId;
    private Float score;
    private Float maxScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuizResultElement> quizResultElements;

    public static QuizResultOutput fromEntity(QuizResult quizResult) {
        return new QuizResultOutput(
                quizResult.getId(),
                quizResult.getQuiz().getId(),
                quizResult.getUser().getId(),
                quizResult.getScore(),
                quizResult.getMaxScore(),
                quizResult.getStartTime(),
                quizResult.getEndTime(),
                quizResult.getCreatedAt(),
                quizResult.getUpdatedAt(),
                null // No elements provided here
        );
    }
    
    public static QuizResultOutput fromEntityAndQuizResultElements(QuizResult quizResult, List<QuizResultElement> quizResultElements) {
        return new QuizResultOutput(
                quizResult.getId(),
                quizResult.getQuiz().getId(),
                quizResult.getUser().getId(),
                quizResult.getScore(),
                quizResult.getMaxScore(),
                quizResult.getStartTime(),
                quizResult.getEndTime(),
                quizResult.getCreatedAt(),
                quizResult.getUpdatedAt(),
                quizResultElements
        );
    }
}
