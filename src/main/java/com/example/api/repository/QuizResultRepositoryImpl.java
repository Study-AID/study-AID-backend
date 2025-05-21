package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.api.entity.QuizResult;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class QuizResultRepositoryImpl implements QuizResultRepositoryCustom {
    @PersistenceContext
    private EntityManager manager;

    // QuizResult에는 lectureId가 없고, lectureId는 quizResult의 quiz에 있는 lectureId를 통해서 가져온다.
    // lectureId를 통해서 quizResult를 가져오는 메서드
    public List<QuizResult> findByLectureId(UUID lectureId) {
        return manager.createQuery(
                        "SELECT qr FROM QuizResult qr " +
                                "JOIN qr.quiz q " +
                                "WHERE q.lecture.id = :lectureId " +
                                "AND qr.deletedAt IS NULL",
                        QuizResult.class)
                .setParameter("lectureId", lectureId)
                .getResultList();
    }

    @Transactional
    public QuizResult createQuizResult(QuizResult quizResult) {
        if (isDuplicated(quizResult.getQuiz().getId(), quizResult.getUser().getId())) {
            throw new IllegalArgumentException(
                    "Quiz result with the same quiz and user already exists"
            );
        }
        manager.persist(quizResult);
        return quizResult;
    }
    private boolean isDuplicated(UUID quizId, UUID userId) {
        return manager.createQuery(
                        "SELECT COUNT(qr) > 0 FROM QuizResult qr " +
                                "WHERE qr.quiz.id = :quizId " +
                                "AND qr.user.id = :userId " +
                                "AND qr.deletedAt IS NULL",
                        Boolean.class)
                .setParameter("quizId", quizId)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
