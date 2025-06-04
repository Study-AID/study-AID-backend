package com.example.api.repository;

import com.example.api.entity.QuizQuestionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizQuestionReportRepository extends JpaRepository<QuizQuestionReport, UUID>, QuizQuestionReportRepositoryCustom {
    Optional<QuizQuestionReport> findByQuizIdAndQuizItemIdAndUserId(UUID quizId, UUID quizItemId, UUID userId);

    List<QuizQuestionReport> findByUserIdOrderByCreatedAtDesc(UUID userId);

    QuizQuestionReport createQuizQuestionReport(QuizQuestionReport quizQuestionReport);

    void deleteById(UUID id);

    Long countByQuizItemId(UUID quizItemId);
}
