package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.QuizResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class QuizResponseRepositoryImpl implements QuizResponseRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(QuizResponseRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    public List<QuizResponse> findByQuizId(UUID quizId) {
        return manager.createQuery(
                        "SELECT qr FROM QuizResponse qr " +
                                "WHERE qr.quiz.id = :quizId " +
                                "AND qr.deletedAt IS NULL " +
                                "ORDER BY qr.createdAt DESC",
                        QuizResponse.class)
                .setParameter("quizId", quizId)
                .getResultList();
    }

    public Optional<QuizResponse> findByQuizItemId(UUID quizItemId) {
        return manager.createQuery(
                        "SELECT qr FROM QuizResponse qr " +
                                "WHERE qr.quizItem.id = :quizItemId " +
                                "AND qr.deletedAt IS NULL",
                        QuizResponse.class)
                .setParameter("quizItemId", quizItemId)
                .getResultStream()
                .findFirst();
    }
    
    @Transactional
    public QuizResponse createQuizResponse(QuizResponse quizResponse) {
        if (isDuplicated(quizResponse.getQuiz().getId(), quizResponse.getQuizItem().getId())) {
            throw new IllegalArgumentException(
                    "Quiz response with the same quiz and quiz item already exists"
            );
        }
        manager.persist(quizResponse);
        return quizResponse;
    }
    
    private boolean isDuplicated(UUID quizId, UUID quizItemId) {
        return manager.createQuery(
                        "SELECT COUNT(qr) > 0 FROM QuizResponse qr " +
                                "WHERE qr.quiz.id = :quizId " +
                                "AND qr.quizItem.id = :quizItemId " +
                                "AND qr.deletedAt IS NULL",
                        Boolean.class)
                .setParameter("quizId", quizId)
                .setParameter("quizItemId", quizItemId)
                .getSingleResult();
    }

    @Transactional
    public QuizResponse updateQuizResponse(QuizResponse quizResponse) {
        return manager.merge(quizResponse);
    }
    
    @Transactional
    public void deleteQuizResponse(UUID quizResponseId) {
        try {
            QuizResponse quizResponse = manager.find(QuizResponse.class, quizResponseId);
            if (quizResponse != null) {
                manager.merge(quizResponse);
                logger.info("Soft deleted quiz with id: {}", quizResponseId);
            } else {
                logger.warn("Quiz response with ID {} not found for deletion", quizResponseId);
            }
        } catch (Exception e) {
            logger.error("Error deleting quizResponse with id: {}", quizResponseId, e.getMessage());
            throw new RuntimeException("Failed to delete quizResponse", e);
        }
    }
}
