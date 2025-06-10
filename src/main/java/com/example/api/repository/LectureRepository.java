package com.example.api.repository;

import com.example.api.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LectureRepository extends JpaRepository<Lecture, UUID>, LectureRepositoryCustom {
    Optional<Lecture> findById(UUID lectureId);
    
    List<Lecture> findByCourseId(UUID courseId);

    List<Lecture> findByUserId(UUID userId);

    Lecture createLecture(Lecture lecture);

    Lecture updateLecture(Lecture lecture);

    void deleteLecture(UUID lectureId);
}
