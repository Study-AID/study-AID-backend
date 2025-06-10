package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.ExamItem;
import com.example.api.entity.enums.QuestionType;

public interface ExamItemRepositoryCustom {
    Optional<ExamItem> findById(UUID id);
    
    List<ExamItem> findByExamId(UUID examId);

    boolean existsByExamIdAndQuestionTypeAndDeletedAtIsNull(UUID examId, QuestionType questionType);

    ExamItem updateExamItem(ExamItem examItem);
}
