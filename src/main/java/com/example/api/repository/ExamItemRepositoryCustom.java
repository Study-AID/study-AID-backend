package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.ExamItem;

public interface ExamItemRepositoryCustom {
    Optional<ExamItem> findById(UUID id);

    List<ExamItem> findByExamId(UUID examId);

    ExamItem updateExamItem(ExamItem examItem);
}
