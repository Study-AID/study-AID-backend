package com.example.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.api.entity.Lecture;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class LectureRepositoryImpl implements LectureRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(LectureRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    public List<Lecture> findByCourseId(UUID courseId) {
        return manager.createQuery(
                        "SELECT l FROM Lecture l " +
                                "WHERE l.course.id = :courseId " +
                                "AND l.deletedAt IS NULL " +
                                "ORDER BY l.title ASC",
                        Lecture.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Transactional
    public Lecture createLecture(Lecture lecture) {
        // NOTE(yoon): i think we have to check for duplicates of lecture title.
        if (isDuplicated(lecture.getCourse().getId(), lecture.getTitle())) {
            throw new IllegalArgumentException(
                    "Lecture with the same title already exists in this course"
            );
        }
        // TODO(yoon): S3에 Lecture Note를 업로드하는 로직을 추가해야 한다.
        


        manager.persist(lecture);
        return lecture;
    }

    @Transactional
    public Lecture updateLecture(Lecture lecture) {
        return manager.merge(lecture);
    }

    private boolean isDuplicated(UUID courseId, String title) {
        Long count = manager.createQuery(
                        "SELECT COUNT(l) " +
                                "FROM Lecture l " +
                                "WHERE l.course.id = :courseId " +
                                "AND l.title = :title " +
                                "AND l.deletedAt IS NULL",
                        Long.class)
                .setParameter("courseId", courseId)
                .setParameter("title", title)
                .getSingleResult();
        return count > 0;
    }

    @Transactional
    public void deleteLecture(UUID lectureId) {
        try {
            Lecture lecture = manager.find(Lecture.class, lectureId);
            if (lecture != null) {
                lecture.setDeletedAt(LocalDateTime.now());
                manager.merge(lecture);
                logger.info("Soft deleted lecture with id: {}", lectureId);
            } else {
                logger.warn("Lecture not found with id: {}", lectureId);
            }
        } catch (Exception e) {
            logger.error("Error deleting lecture with id {}: {}", lectureId, e.getMessage());
            throw new RuntimeException("Failed to delete lecture", e);
        }
    }
}
