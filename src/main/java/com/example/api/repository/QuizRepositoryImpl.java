package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.Quiz;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class QuizRepositoryImpl implements QuizRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(QuizRepositoryImpl.class);
    
    @PersistenceContext
    private EntityManager manager;
    
    public List<Quiz> findByLectureId(UUID lectureId) {
        return manager.createQuery(
                        "SELECT q FROM Quiz q " +
                                "WHERE q.lecture.id = :lectureId " +
                                "AND q.deletedAt IS NULL " +
                                "ORDER BY q.title ASC",
                        Quiz.class)
                .setParameter("lectureId", lectureId)
                .getResultList();
    }

    public List<Quiz> findByUserId(UUID userId) {
        return manager.createQuery(
                        "SELECT q FROM Quiz q " +
                                "WHERE q.user.id = :userId " +
                                "AND q.deletedAt IS NULL " +
                                "ORDER BY q.title ASC",
                        Quiz.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        // NOTE(yoon): i think we have to check for duplicates of quiz title.
        if (isDuplicated(quiz.getLecture().getId(), quiz.getTitle())) {
            throw new IllegalArgumentException(
                    "Quiz with the same title already exists in this lecture"
            );
        }        
        manager.persist(quiz);
        return quiz;
    }
    
    @Transactional
    public Quiz updateQuiz(Quiz quiz) {
        return manager.merge(quiz);
    }

    private boolean isDuplicated(UUID lectureId, String title) {
        Long count = manager.createQuery(
                        "SELECT COUNT(q) " +
                                "FROM Quiz q " +
                                "WHERE q.lecture.id = :lectureId " +
                                "AND q.title = :title " +
                                "AND q.deletedAt IS NULL",
                        Long.class)
                .setParameter("lectureId", lectureId)
                .setParameter("title", title)
                .getSingleResult();
        return count > 0;
    }

    @Transactional
    public void deleteQuiz(UUID quizId) {
        try {
            Quiz quiz = manager.find(Quiz.class, quizId);
            if (quiz != null) {
                quiz.setDeletedAt(LocalDateTime.now());
                manager.merge(quiz);
                logger.info("Soft deleted quiz with id: {}", quizId);
            } else {
                logger.warn("Quiz not found with id: {}", quizId);
            }
        } catch (Exception e) {
            logger.error("Error deleting quiz with id: {}", quizId, e.getMessage());
            throw new RuntimeException("Failed to delete quiz", e);
        }
    }
}
