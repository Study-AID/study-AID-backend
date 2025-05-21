package com.example.api.repository;

import com.example.api.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID>, ExamRepositoryCustom {
    List<Exam> findByCourseId(UUID courseId);

    List<Exam> findByUserId(UUID courseId);

    Exam createExam(Exam exam);

    Exam updateExam(Exam exam);

    void deleteExam(UUID examId);
}