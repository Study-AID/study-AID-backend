package com.example.api.controller.dto.exam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.api.service.dto.exam.ExamListOutput;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of exams response")
public class ExamListResponse {
    @Schema(description = "List of exams response")
    private List<ExamResponse> exams;

    public static ExamListResponse fromServiceDto(ExamListOutput examListOutput) {
        return new ExamListResponse(
                examListOutput.getExams().stream()
                        .map(ExamResponse::fromServiceDto)
                        .toList()
        );
    }
}
