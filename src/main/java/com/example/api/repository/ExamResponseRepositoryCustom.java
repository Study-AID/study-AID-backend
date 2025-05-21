package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import com.example.api.entity.ExamResponse;

public interface ExamResponseRepositoryCustom {
    List<ExamResponse> findByExamId(UUID examId);

    ExamResponse createExamResponse(ExamResponse examResponse);

    ExamResponse updateExamResponse(ExamResponse examResponse);

    void deleteExamResponse(UUID examResponseId);
}
