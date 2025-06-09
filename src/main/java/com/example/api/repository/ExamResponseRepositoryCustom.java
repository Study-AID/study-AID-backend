package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.ExamResponse;

public interface ExamResponseRepositoryCustom {
    List<ExamResponse> findByExamId(UUID examId);

    Optional<ExamResponse> findByExamItemId(UUID examItemId);

    ExamResponse createExamResponse(ExamResponse examResponse);

    ExamResponse updateExamResponse(ExamResponse examResponse);

    void deleteExamResponse(UUID examResponseId);
}
