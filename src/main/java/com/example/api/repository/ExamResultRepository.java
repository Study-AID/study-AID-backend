package com.example.api.repository;

import com.example.api.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamResultRepository extends JpaRepository<ExamResult, UUID>, ExamResultRepositoryCustom {
    Optional<ExamResult> findByExamId(UUID examId);
    
    List<ExamResult> findByCourseId(UUID courseId);

    ExamResult createExamResult(ExamResult examResult);
}

