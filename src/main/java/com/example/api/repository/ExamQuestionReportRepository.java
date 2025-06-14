package com.example.api.repository;

import com.example.api.entity.ExamQuestionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamQuestionReportRepository extends JpaRepository<ExamQuestionReport, UUID>, ExamQuestionReportRepositoryCustom {
    Optional<ExamQuestionReport> findByExamIdAndExamItemIdAndUserId(UUID examId, UUID examItemId, UUID userId);

    List<ExamQuestionReport> findByUserIdOrderByCreatedAtDesc(UUID userId);

    ExamQuestionReport createExamQuestionReport(ExamQuestionReport examQuestionReport);

    void deleteById(UUID id);

    Long countByExamItemId(UUID examItemId);
}
