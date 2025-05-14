package com.example.api.service.dto.quiz;

import java.util.List;
import java.util.stream.Collectors;

import com.example.api.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizListOutput {
    private List<QuizOutput> quizzes;

    public static QuizListOutput fromEntities(List<Quiz> quizzes) {
        List<QuizOutput> quizOutputs = quizzes.stream()
                .map(QuizOutput::fromEntity)
                .collect(Collectors.toList());
        return new QuizListOutput(quizOutputs);
    }
}
