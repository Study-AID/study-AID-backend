package com.example.api.repository;

import com.example.api.entity.ExamResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamResponseRepository extends JpaRepository<ExamResponse, UUID>, ExamResponseRepositoryCustom {
    List<ExamResponse> findByExamId(UUID examId);

    Optional<ExamResponse> findByExamItemId(UUID examItemId);

    ExamResponse createExamResponse(ExamResponse examResponse);

    ExamResponse updateExamResponse(ExamResponse examResponse);

    void deleteExamResponse(UUID examResponseId);
}
