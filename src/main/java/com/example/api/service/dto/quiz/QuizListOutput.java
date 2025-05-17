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
                .map(quiz -> new QuizOutput(
                        quiz.getId(),
                        quiz.getLecture().getId(),
                        quiz.getUser().getId(),
                        quiz.getTitle(),
                        quiz.getStatus(),
                        quiz.getContentsGenerateAt(),
                        quiz.getCreatedAt(),
                        quiz.getUpdatedAt(),
                        null
                ))
                .collect(Collectors.toList());
        return new QuizListOutput(quizOutputs);
    }
}
