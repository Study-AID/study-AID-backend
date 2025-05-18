package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import com.example.api.entity.Exam;

public interface ExamRepositoryCustom {
    List<Exam> findByCourseId(UUID courseId);

    Exam createExam(Exam exam);

    Exam updateExam(Exam exam);

    void deleteExam(UUID examId);
}
