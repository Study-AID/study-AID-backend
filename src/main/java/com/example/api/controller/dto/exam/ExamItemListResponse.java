package com.example.api.controller.dto.exam;

import java.util.List;

import com.example.api.service.dto.exam.ExamItemListOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exam Item List response DTO")
public class ExamItemListResponse {
    @Schema(description = "List of exam items")
    private List<ExamItemResponse> examItems;

    public static ExamItemListResponse fromServiceDto(ExamItemListOutput examItemListOutput) {
        return new ExamItemListResponse(
            examItemListOutput.getExamItems().stream()
                .map(ExamItemResponse::fromServiceDto)
                .toList()
        );
    }
}
