package com.example.api.service.dto.report;

import com.example.api.entity.QuizQuestionReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionReportOutput {
    private UUID id;
    private UUID userId;
    private UUID quizId;
    private UUID quizItemId;
    private String reportReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizQuestionReportOutput fromEntity(QuizQuestionReport report) {
        return new QuizQuestionReportOutput(
                report.getId(),
                report.getUser().getId(),
                report.getQuiz().getId(),
                report.getQuizItem().getId(),
                report.getReportReason(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}