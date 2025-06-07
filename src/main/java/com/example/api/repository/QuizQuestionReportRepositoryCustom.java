package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.QuizQuestionReport;

public interface QuizQuestionReportRepositoryCustom {
    Optional<QuizQuestionReport> findByQuizIdAndQuizItemIdAndUserId(UUID quizId, UUID quizItemId, UUID userId);

    List<QuizQuestionReport> findByUserIdOrderByCreatedAtDesc(UUID userId);

    QuizQuestionReport createQuizQuestionReport(QuizQuestionReport quizQuestionReport);

    void deleteById(UUID id);
    
    Long countByQuizItemId(UUID quizItemId);
}
