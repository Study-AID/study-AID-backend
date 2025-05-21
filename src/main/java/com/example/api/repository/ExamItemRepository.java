package com.example.api.repository;

import com.example.api.entity.ExamItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamItemRepository extends JpaRepository<ExamItem, UUID> {
    List<ExamItem> findByExamId(UUID examId);   
}
