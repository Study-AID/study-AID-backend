package com.example.api.controller.dto.quiz;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.service.dto.quiz.QuizResultOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quiz Result response DTO")
public class QuizResultResponse {
    @Schema(description = "Quiz Result ID")
    private UUID id;

    @Schema(description = "Quiz ID")
    private UUID quizId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Score")
    private Float score;

    @Schema(description = "Maximum Score")
    private Float maxScore;

    @Schema(description = "Feedback")
    private String feedback;

    @Schema(description = "Start Time")
    private LocalDateTime startTime;

    @Schema(description = "End Time")
    private LocalDateTime endTime;

    @Schema(description = "Creation Time")
    private LocalDateTime createdAt;

    @Schema(description = "Last Update Time")
    private LocalDateTime updatedAt;

    public static QuizResultResponse fromServiceDto(QuizResultOutput quizResultOutput) {
        return new QuizResultResponse(
                quizResultOutput.getId(),
                quizResultOutput.getQuizId(),
                quizResultOutput.getUserId(),
                quizResultOutput.getScore(),
                quizResultOutput.getMaxScore(),
                quizResultOutput.getFeedback(),
                quizResultOutput.getStartTime(),
                quizResultOutput.getEndTime(),
                quizResultOutput.getCreatedAt(),
                quizResultOutput.getUpdatedAt()
        );
    }
}
