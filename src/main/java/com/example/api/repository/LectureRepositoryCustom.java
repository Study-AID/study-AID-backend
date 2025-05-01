package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import com.example.api.entity.Lecture;

public interface LectureRepositoryCustom {
    List<Lecture> findByCourseId(UUID courseId);

    Lecture createLecture(Lecture lecture);

    Lecture updateLecture(Lecture lecture);

    void deleteLecture(UUID lectureId);
}
