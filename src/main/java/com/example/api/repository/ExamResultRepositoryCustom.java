package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import com.example.api.entity.ExamResult;

public interface ExamResultRepositoryCustom {
    List<ExamResult> findByCourseId(UUID courseId);

    ExamResult createExamResult(ExamResult examResult);
}
