package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.ExamQuestionReport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class ExamQuestionReportRepositoryImpl implements ExamQuestionReportRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(ExamQuestionReportRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Optional<ExamQuestionReport> findByExamIdAndExamItemIdAndUserId(UUID examId, UUID examItemId, UUID userId) {
        return manager.createQuery(
                        "SELECT e FROM ExamQuestionReport e " +
                                "WHERE e.exam.id = :examId " +
                                "AND e.examItem.id = :examItemId " +
                                "AND e.user.id = :userId " +
                                "AND e.deletedAt IS NULL",
                        ExamQuestionReport.class)
                .setParameter("examId", examId)
                .setParameter("examItemId", examItemId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<ExamQuestionReport> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return manager.createQuery(
                        "SELECT e FROM ExamQuestionReport e " +
                                "WHERE e.user.id = :userId " +
                                "AND e.deletedAt IS NULL " +
                                "ORDER BY e.createdAt DESC",
                        ExamQuestionReport.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @Transactional
    public ExamQuestionReport createExamQuestionReport(ExamQuestionReport examQuestionReport) {
        manager.persist(examQuestionReport);
        return examQuestionReport;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        try {
            ExamQuestionReport report = manager.find(ExamQuestionReport.class, id);
            if (report != null) {
                report.setDeletedAt(LocalDateTime.now());
                manager.merge(report);
            } else {
                logger.warn("Attempted to delete non-existing ExamQuestionReport with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error deleting ExamQuestionReport with ID: {}", id, e);
            throw new RuntimeException("Error deleting ExamQuestionReport with ID: ", e);
        }
    }

    @Override
    public Long countByExamItemId(UUID examItemId) {
        return manager.createQuery(
                        "SELECT COUNT(e) FROM ExamQuestionReport e " +
                                "WHERE e.examItem.id = :examItemId " +
                                "AND e.deletedAt IS NULL", Long.class)
                .setParameter("examItemId", examItemId)
                .getSingleResult();
    }
}
