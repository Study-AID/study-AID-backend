package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.QuizQuestionReport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class QuizQuestionReportRepositoryImpl implements QuizQuestionReportRepositoryCustom{
    private static final Logger logger = LoggerFactory.getLogger(QuizQuestionReportRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    @Override
    public Optional<QuizQuestionReport> findByQuizIdAndQuizItemIdAndUserId(UUID quizId, UUID quizItemId, UUID userId) {
        return manager.createQuery(
                        "SELECT q FROM QuizQuestionReport q " +
                                "WHERE q.quiz.id = :quizId " +
                                "AND q.quizItem.id = :quizItemId " +
                                "AND q.user.id = :userId " +
                                "AND q.deletedAt IS NULL",
                        QuizQuestionReport.class)
                .setParameter("quizId", quizId)
                .setParameter("quizItemId", quizItemId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<QuizQuestionReport> findByQuizItemIdOrderByCreatedAtDesc(UUID quizItemId) {
        return manager.createQuery(
                        "SELECT q FROM QuizQuestionReport q " +
                                "WHERE q.quizItem.id = :quizItemId " +
                                "AND q.deletedAt IS NULL " +
                                "ORDER BY q.createdAt DESC",
                        QuizQuestionReport.class)
                .setParameter("quizItemId", quizItemId)
                .getResultList();
    }

    @Override
    public List<QuizQuestionReport> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return manager.createQuery(
                        "SELECT q FROM QuizQuestionReport q " +
                                "WHERE q.user.id = :userId " +
                                "AND q.deletedAt IS NULL " +
                                "ORDER BY q.createdAt DESC",
                        QuizQuestionReport.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @Transactional
    public QuizQuestionReport createQuizQuestionReport(QuizQuestionReport quizQuestionReport) {
        manager.persist(quizQuestionReport);
        return quizQuestionReport;
    }

    @Override
    @Transactional
    public void deleteById(UUID reportId) {
        try {
            QuizQuestionReport report = manager.find(QuizQuestionReport.class, reportId);
            if (report != null) {
                report.setDeletedAt(LocalDateTime.now());
                manager.merge(report);
                logger.info("Soft deleted report with id: {}", reportId);
            } else {
                logger.warn("Attempted to delete non-existing QuizQuestionReport with ID: {}", reportId);
            }
        } catch (Exception e) {
            logger.error("Error deleting QuizQuestionReport with ID: {}", reportId, e);
            throw new RuntimeException("Failed to delete report", e);
        }
    }

    @Override
    public Long countByQuizItemId(UUID quizItemId) {
        return manager.createQuery(
                        "SELECT COUNT(q) FROM QuizQuestionReport q " +
                                "WHERE q.quizItem.id = :quizItemId " +
                                "AND q.deletedAt IS NULL",
                        Long.class)
                .setParameter("quizItemId", quizItemId)
                .getSingleResult();
    }
}
