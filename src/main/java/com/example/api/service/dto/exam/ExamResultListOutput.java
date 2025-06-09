package com.example.api.service.dto.exam;

import java.util.List;
import java.util.stream.Collectors;

import com.example.api.entity.ExamResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultListOutput {
    private List<ExamResultOutput> examResults;

    public static ExamResultListOutput fromEntities(List<ExamResult> examResults) {
        List<ExamResultOutput> examResultOutputs = examResults.stream()
                .map(ExamResultOutput::fromEntity)
                .collect(Collectors.toList());
        return new ExamResultListOutput(examResultOutputs);
    }
}
