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
    private EntityManager entityManager;

    public Optional<ExamItem> findById(UUID id) {
        return entityManager.createQuery(
                "SELECT ei FROM ExamItem ei WHERE ei.id = :id AND ei.deletedAt IS NULL",
                ExamItem.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<ExamItem> findByExamId(UUID examId) {
        return entityManager.createQuery(
                "SELECT ei FROM ExamItem ei WHERE ei.exam.id = :examId AND ei.deletedAt IS NULL",
                ExamItem.class)
                .setParameter("examId", examId)
                .getResultList();
    }

    @Transactional
    public ExamItem updateExamItem(ExamItem examItem) {
        // Assuming that the examItem is already managed by the entity manager
        examItem.setUpdatedAt(LocalDateTime.now());
        return entityManager.merge(examItem);
    }
}
