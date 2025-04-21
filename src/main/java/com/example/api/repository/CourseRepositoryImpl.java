package com.example.api.repository;

import com.example.api.entity.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class CourseRepositoryImpl implements CourseRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(CourseRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    public List<Course> findBySemesterId(UUID semesterId) {
        return manager.createQuery(
                        "SELECT c FROM Course c " +
                                "WHERE c.semester.id = :semesterId " +
                                "AND c.deletedAt IS NULL " +
                                "ORDER BY c.name ASC",
                        Course.class)
                .setParameter("semesterId", semesterId)
                .getResultList();
    }

    @Transactional
    public Course createCourse(Course course) {
        // NOTE(mj): we may don't have to check for duplicates of course name.
        if (isDuplicated(course.getSemester().getId(), course.getName())) {
            throw new IllegalArgumentException(
                    "Course with the same name already exists in this semester"
            );
        }
        manager.persist(course);
        return course;
    }

    @Transactional
    public Course updateCourse(Course course) {
        return manager.merge(course);
    }

    private boolean isDuplicated(UUID semesterId, String name) {
        Long count = manager.createQuery(
                        "SELECT COUNT(c) " +
                                "FROM Course c " +
                                "WHERE c.semester.id = :semesterId " +
                                "AND c.name = :name " +
                                "AND c.deletedAt IS NULL",
                        Long.class)
                .setParameter("semesterId", semesterId)
                .setParameter("name", name)
                .getSingleResult();
        return count > 0;
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        try {
            Course course = manager.find(Course.class, courseId);
            if (course != null) {
                course.setDeletedAt(LocalDateTime.now());
                manager.merge(course);
                logger.info("Soft deleted course with id: {}", courseId);
            } else {
                logger.warn("Course not found with id: {}", courseId);
            }
        } catch (Exception e) {
            logger.error("Error deleting course with id: {}", courseId, e);
            throw new RuntimeException("Failed to delete course", e);
        }
    }
}