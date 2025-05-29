package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.ExamResult;

public interface ExamResultRepositoryCustom {
    Optional<ExamResult> findByExamId(UUID examId);
    
    List<ExamResult> findByCourseId(UUID courseId);

    ExamResult createExamResult(ExamResult examResult);
}
