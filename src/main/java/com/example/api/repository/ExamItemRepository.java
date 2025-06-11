package com.example.api.repository;

import com.example.api.entity.ExamItem;

import com.example.api.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamItemRepository extends JpaRepository<ExamItem, UUID> {
    Optional<ExamItem> findById(UUID id);
    
    List<ExamItem> findByExamId(UUID examId);

    boolean existsByExamIdAndQuestionTypeAndDeletedAtIsNull(UUID examId, QuestionType questionType);

    ExamItem updateExamItem(ExamItem examItem);
}
