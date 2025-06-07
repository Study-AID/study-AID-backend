package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.CourseAssessment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class CourseAssessmentRepositoryImpl implements CourseAssessmentRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(CourseAssessmentRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    public List<CourseAssessment> findByCourseId(UUID courseId) {
        return manager.createQuery(
                        "SELECT ca FROM CourseAssessment ca " +
                                "WHERE ca.course.id = :courseId " +
                                "AND ca.deletedAt IS NULL " +
                                "ORDER BY ca.createdAt DESC",
                        CourseAssessment.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Transactional
    public CourseAssessment createCourseAssessment(CourseAssessment courseAssessment) {
        courseAssessment.setCreatedAt(LocalDateTime.now());
        manager.persist(courseAssessment);
        return courseAssessment;
    }

    @Transactional
    public CourseAssessment updateCourseAssessment(CourseAssessment courseAssessment) {
        courseAssessment.setUpdatedAt(LocalDateTime.now());
        return manager.merge(courseAssessment);
    }

    @Transactional
    public void deleteCourseAssessment(UUID courseAssessmentId) {
        try {
            CourseAssessment courseAssessment = manager.find(CourseAssessment.class, courseAssessmentId);
            if (courseAssessment != null) {
                courseAssessment.setDeletedAt(LocalDateTime.now());
                manager.merge(courseAssessment);
                logger.info("Soft deleted course assessment with id: {}", courseAssessmentId);
            } else {
                logger.warn("Course assessment not found with id: {}", courseAssessmentId);
            }
        } catch (Exception e) {
            logger.error("Error deleting course assessment with id {}: {}", courseAssessmentId, e.getMessage());
            throw new RuntimeException("Failed to delete course assessment", e);
        }
    }
}