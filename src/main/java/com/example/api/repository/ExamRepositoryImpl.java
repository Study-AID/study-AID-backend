package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.api.entity.Exam;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public class ExamRepositoryImpl implements ExamRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(ExamRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;  

    public List<Exam> findByCourseId(UUID courseId) {
        return manager.createQuery(
                        "SELECT e FROM Exam e " +
                                "WHERE e.course.id = :courseId " +
                                "AND e.deletedAt IS NULL " +
                                "ORDER BY e.title ASC",
                        Exam.class)
                .setParameter("courseId", courseId)
                .getResultList();

    }

    @Transactional
    public Exam createExam(Exam exam) {
        if (isDuplicated(exam.getCourse().getId(), exam.getTitle())) {
            throw new IllegalArgumentException(
                    "Exam with the same title already exists in this course"
            );
        }
        manager.persist(exam);
        return exam;
    }

    private boolean isDuplicated(UUID courseId, String title) {
        return manager.createQuery(
                        "SELECT COUNT(e) FROM Exam e " +
                                "WHERE e.course.id = :courseId " +
                                "AND e.title = :title " +
                                "AND e.deletedAt IS NULL",
                        Long.class)
                .setParameter("courseId", courseId)
                .setParameter("title", title)
                .getSingleResult() > 0;
    }

    @Transactional
    public Exam updateExam(Exam exam) {
        return manager.merge(exam);
    }

    @Transactional
    public void deleteExam(UUID examId) {
        try {
            Exam exam = manager.find(Exam.class, examId);
            if (exam != null) {
                exam.setDeletedAt(LocalDateTime.now());
                manager.merge(exam);
                logger.info("Exam with ID: " + examId + " deleted successfully.");
            } else {
                logger.warn("Exam with ID: " + examId + " not found.");
            }
        } catch (Exception e) {
            logger.error("Error deleting exam with ID: " + examId, e);
            throw new RuntimeException("Error deleting exam with ID: " + examId, e);
        }
    }
}
