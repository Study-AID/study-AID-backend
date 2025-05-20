package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.api.entity.ExamResult;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class ExamResultRepositoryImpl implements ExamResultRepositoryCustom {
    @PersistenceContext
    private EntityManager manager;

    public List<ExamResult> findByCourseId(UUID courseId) {
        return manager.createQuery(
                        "SELECT er FROM ExamResult er " +
                                "JOIN er.exam e " +
                                "WHERE e.course.id = :courseId " +
                                "AND er.deletedAt IS NULL",
                        ExamResult.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Transactional
    public ExamResult createExamResult(ExamResult examResult) {
        if (isDuplicated(examResult.getExam().getId(), examResult.getUser().getId())) {
            throw new IllegalArgumentException(
                    "Exam result with the same exam and user already exists"
            );
        }
        manager.persist(examResult);
        return examResult;
    }
    private boolean isDuplicated(UUID examId, UUID userId) {
        return manager.createQuery(
                        "SELECT COUNT(er) > 0 FROM ExamResult er " +
                                "WHERE er.exam.id = :examId " +
                                "AND er.user.id = :userId " +
                                "AND er.deletedAt IS NULL",
                        Boolean.class)
                .setParameter("examId", examId)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
