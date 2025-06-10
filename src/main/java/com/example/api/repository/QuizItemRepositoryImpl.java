package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.api.entity.QuizItem;
import com.example.api.entity.enums.QuestionType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class QuizItemRepositoryImpl implements QuizItemRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    public Optional<QuizItem> findById(UUID id) {
        return entityManager.createQuery(
                "SELECT qi FROM QuizItem qi WHERE qi.id = :id AND qi.deletedAt IS NULL",
                QuizItem.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<QuizItem> findByQuizId(UUID quizId) {
        return entityManager.createQuery(
                "SELECT qi FROM QuizItem qi WHERE qi.quiz.id = :quizId AND qi.deletedAt IS NULL",
                QuizItem.class)
                .setParameter("quizId", quizId)
                .getResultList();
    }

    public boolean existsByQuizIdAndQuestionTypeAndDeletedAtIsNull(UUID quizId, QuestionType questionType) {
        Long count = entityManager.createQuery(
                "SELECT COUNT(qi) FROM QuizItem qi WHERE qi.quiz.id = :quizId AND qi.questionType = :questionType AND qi.deletedAt IS NULL",
                Long.class)
                .setParameter("quizId", quizId)
                .setParameter("questionType", questionType)
                .getSingleResult();
        return count > 0;
    }

    @Transactional
    public QuizItem updateQuizItem(QuizItem quizItem) {
        // Assuming that the quizItem is already managed by the entity manager
        quizItem.setUpdatedAt(LocalDateTime.now());
        return entityManager.merge(quizItem);
    }
}
