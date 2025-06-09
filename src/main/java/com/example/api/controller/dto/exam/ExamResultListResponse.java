package com.example.api.controller.dto.exam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.service.dto.exam.ExamResultListOutput;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of exam results response")
public class ExamResultListResponse {
    @Schema(description = "List of exam results response")
    private List<ExamResultResponse> examResults;

    public static ExamResultListResponse fromServiceDto(ExamResultListOutput examResultListOutput) {
        List<ExamResultResponse> examResultResponses = examResultListOutput.getExamResults().stream()
                .map(ExamResultResponse::fromServiceDto)
                .toList();
        return new ExamResultListResponse(examResultResponses);
    }
}
