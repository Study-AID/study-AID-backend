package com.example.api.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.service.dto.lecture.*;

import jakarta.transaction.Transactional;

@Service
public interface LectureService {
    Optional<LectureOutput> findLectureById(UUID lectureId);

    LectureListOutput findLecturesByCourseId(UUID courseId);

    @Transactional
    LectureOutput createLecture(CreateLectureInput input);

    @Transactional
    LectureOutput updateLecture(UpdateLectureInput input);

    @Transactional
    LectureOutput updateLectureSummaryStatus(UpdateLectureSummaryStatusInput input);

    @Transactional
    LectureOutput updateLectureDisplayOrderLex(UpdateLectureDisplayOrderLexInput input);

    @Transactional
    void deleteLecture(UUID lectureId);
}
