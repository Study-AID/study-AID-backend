package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.api.entity.ExamItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class ExamItemRepositoryImpl {
    @PersistenceContext
    private EntityManager manager;
    
    public Optional<ExamItem> findById(UUID id) {
        return manager.createQuery(
                "SELECT ei FROM ExamItem ei WHERE ei.id = :id AND ei.deletedAt IS NULL",
                ExamItem.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<ExamItem> findByExamId(UUID examId) {
        return manager.createQuery(
                "SELECT ei FROM ExamItem ei WHERE ei.exam.id = :examId AND ei.deletedAt IS NULL",
                ExamItem.class)
                .setParameter("examId", examId)
                .getResultList();
    }

    public boolean existsByExamIdAndQuestionTypeAndDeletedAtIsNull(UUID examId, String questionType) {
        Long count = manager.createQuery(
                "SELECT COUNT(ei) FROM ExamItem ei WHERE ei.exam.id = :examId AND ei.questionType = :questionType AND ei.deletedAt IS NULL",
                Long.class)
                .setParameter("examId", examId)
                .setParameter("questionType", questionType)
                .getSingleResult();
        return count > 0;
    }

    @Transactional
    public ExamItem updateExamItem(ExamItem examItem) {
        try {
            examItem.setUpdatedAt(LocalDateTime.now());
            return manager.merge(examItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ExamItem", e);
        }
    }
}
