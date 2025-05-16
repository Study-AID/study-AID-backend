package com.example.api.repository;

import java.util.List;
import java.util.UUID;
import com.example.api.entity.QuizResponse;

public interface QuizResponseRepositoryCustom {
    List<QuizResponse> findByQuizId(UUID quizId);

    QuizResponse createQuizResponse(QuizResponse quizResponse);

    QuizResponse updateQuizResponse(QuizResponse quizResponse);

    void deleteQuizResponse(UUID quizResponseId);
}
