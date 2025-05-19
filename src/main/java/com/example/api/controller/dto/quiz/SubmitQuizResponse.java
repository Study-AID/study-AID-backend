package com.example.api.controller.dto.quiz;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.service.dto.quiz.QuizResponseOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quiz response DTO")
public class SubmitQuizResponse {
    @Schema(description = "Quiz response ID")
    private UUID id;

    @Schema(description = "Quiz ID")
    private UUID quizId;

    @Schema(description = "Quiz item ID")
    private UUID quizItemId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time")
    private LocalDateTime updatedAt;

    public static SubmitQuizResponse fromServiceDto(QuizResponseOutput quiz) {
        return new SubmitQuizResponse(
                quiz.getId(),
                quiz.getQuizId(),
                quiz.getQuizItemId(),
                quiz.getUserId(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }
}
