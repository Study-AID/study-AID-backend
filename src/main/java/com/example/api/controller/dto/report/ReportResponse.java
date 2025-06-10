package com.example.api.controller.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.service.dto.report.QuizQuestionReportOutput;
import com.example.api.service.dto.report.ExamQuestionReportOutput;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private UUID id;
    private UUID userId;
    private String itemType; // "QUIZ" or "EXAM"
    private UUID quizId;
    private UUID quizItemId;
    private UUID examId;
    private UUID examItemId;
    private String reportReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse fromQuizReportServiceDto(QuizQuestionReportOutput quizReport) {
        ReportResponse response = new ReportResponse();
        response.setId(quizReport.getId());
        response.setUserId(quizReport.getUserId());
        response.setItemType("QUIZ");
        response.setQuizId(quizReport.getQuizId());
        response.setQuizItemId(quizReport.getQuizItemId());
        response.setReportReason(quizReport.getReportReason());
        response.setCreatedAt(quizReport.getCreatedAt());
        response.setUpdatedAt(quizReport.getUpdatedAt());
        return response;
    }

    public static ReportResponse fromExamReportServiceDto(ExamQuestionReportOutput examReport) {
        ReportResponse response = new ReportResponse();
        response.setId(examReport.getId());
        response.setUserId(examReport.getUserId());
        response.setItemType("EXAM");
        response.setExamId(examReport.getExamId());
        response.setExamItemId(examReport.getExamItemId());
        response.setReportReason(examReport.getReportReason());
        response.setCreatedAt(examReport.getCreatedAt());
        response.setUpdatedAt(examReport.getUpdatedAt());
        return response;
    }
}