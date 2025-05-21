package com.example.api.service.dto.exam;

import java.util.List;
import java.util.stream.Collectors;

import com.example.api.entity.ExamResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseListOutput {
    private List<ExamResponseOutput> examResponseOutputs;

    public static ExamResponseListOutput fromEntities(List<ExamResponse> examResponses) {
        List<ExamResponseOutput> examResponseOutputs = examResponses.stream()
                .map(examResponse -> new ExamResponseOutput(
                        examResponse.getId(),
                        examResponse.getExam().getId(),
                        examResponse.getExamItem().getId(),
                        examResponse.getUser().getId(),
                        examResponse.getCreatedAt(),
                        examResponse.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        return new ExamResponseListOutput(examResponseOutputs);
    }
}
