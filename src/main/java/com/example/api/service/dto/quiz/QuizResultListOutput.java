package com.example.api.service.dto.quiz;

import java.util.List;
import java.util.stream.Collectors;

import com.example.api.entity.QuizResult;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultListOutput {
    private List<QuizResultOutput> quizResults;

    public static QuizResultListOutput fromEntities(List<QuizResult> quizResults) {
        List<QuizResultOutput> quizResultOutputs = quizResults.stream()
                .map(QuizResultOutput::fromEntity)
                .collect(Collectors.toList());
        return new QuizResultListOutput(quizResultOutputs);
    }
}
