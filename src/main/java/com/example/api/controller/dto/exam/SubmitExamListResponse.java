package com.example.api.controller.dto.exam;
import java.util.List;

import com.example.api.service.dto.exam.ExamResponseListOutput;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitExamListResponse {
    List<SubmitExamResponse> submitExamResponses;

    public static SubmitExamListResponse fromServiceDto(ExamResponseListOutput examResponseListOutput) {
        return new SubmitExamListResponse(
            examResponseListOutput.getExamResponseOutputs().stream()
                        .map(SubmitExamResponse::fromServiceDto)
                        .toList()
        );
    }
}
