package com.example.api.repository;

import com.example.api.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, UUID>, QuizResponseRepositoryCustom {
    List<QuizResponse> findByQuizId(UUID quizId);

    Optional<QuizResponse> findByQuizItemId(UUID quizItemId);

    QuizResponse createQuizResponse(QuizResponse quizResponse);

    QuizResponse updateQuizResponse(QuizResponse quizResponse);

    void deleteQuizResponse(UUID quizResponseId);
}
