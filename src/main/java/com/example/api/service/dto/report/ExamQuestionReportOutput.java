package com.example.api.service.dto.report;

import com.example.api.entity.ExamQuestionReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionReportOutput {
    private UUID id;
    private UUID userId;
    private UUID examId;
    private UUID examItemId;
    private String reportReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExamQuestionReportOutput fromEntity(ExamQuestionReport report) {
        return new ExamQuestionReportOutput(
                report.getId(),
                report.getUser().getId(),
                report.getExam().getId(),
                report.getExamItem().getId(),
                report.getReportReason(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
