package com.example.api.service.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateQuizQuestionReportInput {
    private UUID userId;
    private UUID quizId;
    private UUID quizItemId;
    private String reportReason;
}