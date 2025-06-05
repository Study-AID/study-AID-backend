package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.example.api.entity.QuizResponse;

public interface QuizResponseRepositoryCustom {
    List<QuizResponse> findByQuizId(UUID quizId);

    Optional<QuizResponse> findByQuizItemId(UUID quizItemId);

    QuizResponse createQuizResponse(QuizResponse quizResponse);

    QuizResponse updateQuizResponse(QuizResponse quizResponse);

    void deleteQuizResponse(UUID quizResponseId);
}
