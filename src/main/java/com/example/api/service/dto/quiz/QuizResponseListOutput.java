package com.example.api.service.dto.quiz;

import java.util.List;
import java.util.stream.Collectors;

import com.example.api.entity.QuizResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseListOutput {
    private List<QuizResponseOutput> quizResponses;

    public static QuizResponseListOutput fromEntities(List<QuizResponse> quizResponses) {
        List<QuizResponseOutput> quizResponseOutputs = quizResponses.stream()
                .map(quizResponse -> new QuizResponseOutput(
                        quizResponse.getId(),
                        quizResponse.getQuiz().getId(),
                        quizResponse.getQuizItem().getId(),
                        quizResponse.getUser().getId(),
                        quizResponse.getCreatedAt(),
                        quizResponse.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        return new QuizResponseListOutput(quizResponseOutputs);
    }
}
