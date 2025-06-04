package com.example.api.controller.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateReportRequest {
    private String itemType; // "QUIZ" or "EXAM"
    private UUID quizId;
    private UUID quizItemId;
    private UUID examId;
    private UUID examItemId;
    private String reportReason;
}