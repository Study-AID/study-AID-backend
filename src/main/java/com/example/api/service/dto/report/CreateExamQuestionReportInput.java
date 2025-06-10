package com.example.api.service.dto.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateExamQuestionReportInput {
    private UUID userId;
    private UUID examId;
    private UUID examItemId;
    private String reportReason;
}
