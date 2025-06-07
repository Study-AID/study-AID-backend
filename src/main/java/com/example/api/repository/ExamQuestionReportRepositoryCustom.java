package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.ExamQuestionReport;

public interface ExamQuestionReportRepositoryCustom {
    Optional<ExamQuestionReport> findByExamIdAndExamItemIdAndUserId(UUID examId, UUID examItemId, UUID userId);

    List<ExamQuestionReport> findByUserIdOrderByCreatedAtDesc(UUID userId);

    ExamQuestionReport createExamQuestionReport(ExamQuestionReport examQuestionReport);

    void deleteById(UUID id);

    Long countByExamItemId(UUID examItemId);
}
