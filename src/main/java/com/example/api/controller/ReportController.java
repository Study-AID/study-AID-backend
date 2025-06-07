package com.example.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.controller.dto.report.*;
import com.example.api.service.QuizQuestionReportService;
import com.example.api.service.ExamQuestionReportService;
import com.example.api.service.dto.report.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/v1/reports")
@Tag(name = "report", description = "Report API")
@Slf4j
public class ReportController extends BaseController {
    private final QuizQuestionReportService quizQuestionReportService;
    private final ExamQuestionReportService examQuestionReportService;

    public ReportController(
            QuizQuestionReportService quizQuestionReportService,
            ExamQuestionReportService examQuestionReportService
    ) {
        this.quizQuestionReportService = quizQuestionReportService;
        this.examQuestionReportService = examQuestionReportService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new report",
            description = "Create a new report for a quiz item or exam item.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Report creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateReportRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Report created successfully",
                            content = @Content(schema = @Schema(implementation = ReportResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Quiz or Exam not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<ReportResponse> createReport(
            @org.springframework.web.bind.annotation.RequestBody CreateReportRequest request
    ) {
        UUID userId = getAuthenticatedUserId();

        try {
            // Validate request
            if (request.getItemType() == null || request.getReportReason() == null || request.getReportReason().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            if ("QUIZ".equals(request.getItemType())) {
                // Handle Quiz Report
                if (request.getQuizId() == null || request.getQuizItemId() == null) {
                    return ResponseEntity.badRequest().build();
                }

                CreateQuizQuestionReportInput input = new CreateQuizQuestionReportInput();
                input.setUserId(userId);
                input.setQuizId(request.getQuizId());
                input.setQuizItemId(request.getQuizItemId());
                input.setReportReason(request.getReportReason());

                QuizQuestionReportOutput output = quizQuestionReportService.createReport(input);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ReportResponse.fromQuizReportServiceDto(output));

            } else if ("EXAM".equals(request.getItemType())) {
                if (request.getExamId() == null || request.getExamItemId() == null) {
                    return ResponseEntity.badRequest().build();
                }
                
                CreateExamQuestionReportInput input = new CreateExamQuestionReportInput();
                input.setUserId(userId);
                input.setExamId(request.getExamId());
                input.setExamItemId(request.getExamItemId());
                input.setReportReason(request.getReportReason());
                
                ExamQuestionReportOutput output = examQuestionReportService.createReport(input);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ReportResponse.fromExamReportServiceDto(output));
            } else {
                return ResponseEntity.badRequest().build();
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-reports")
    @Operation(
            summary = "Get my reports",
            description = "Retrieve all reports created by the authenticated user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reports retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ReportListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<ReportListResponse> getMyReports() {
        UUID userId = getAuthenticatedUserId();

        try {
            List<ReportResponse> allReports = new ArrayList<>();

            // Get Quiz Reports
            List<QuizQuestionReportOutput> quizReports = quizQuestionReportService.findReportsByUser(userId);
            allReports.addAll(quizReports.stream()
                    .map(ReportResponse::fromQuizReportServiceDto)
                    .toList());

            List<ExamQuestionReportOutput> examReports = examQuestionReportService.findReportsByUser(userId);
            allReports.addAll(examReports.stream()
                    .map(ReportResponse::fromExamReportServiceDto)
                    .toList());

            return ResponseEntity.ok(new ReportListResponse(allReports));

        } catch (Exception e) {
            log.error("Error retrieving user reports for user: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{reportId}")
    @Operation(
            summary = "Delete a report",
            description = "Delete a specific report. Only the creator of the report can delete it.",
            parameters = {
                    @Parameter(
                            name = "reportId",
                            description = "ID of the report to delete",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Report deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User does not have permission to delete this report"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Report not found"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error"
                    )
            }
    )
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        UUID userId = getAuthenticatedUserId();

        // First, check if the report exists
        Optional<QuizQuestionReportOutput> quizReportOpt = quizQuestionReportService.findReportById(reportId);
        Optional<ExamQuestionReportOutput> examReportOpt = examQuestionReportService.findReportById(reportId);
        if (quizReportOpt.isEmpty() && examReportOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if the report belongs to the user
        if (quizReportOpt.isPresent() && !quizReportOpt.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (examReportOpt.isPresent() && !examReportOpt.get().getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Boolean isQuizQuestionReportDeleted;
        Boolean isExamQuestionReportDeleted;
        isQuizQuestionReportDeleted = quizQuestionReportService.deleteReport(reportId);
        isExamQuestionReportDeleted = examQuestionReportService.deleteReport(reportId);

        if (isQuizQuestionReportDeleted || isExamQuestionReportDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            // If neither service deleted the report, it might not exist or the user doesn't have permission
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}