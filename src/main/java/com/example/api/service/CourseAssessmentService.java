package com.example.api.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.service.dto.courseAssessment.*;

import jakarta.transaction.Transactional;

@Service
public interface CourseAssessmentService {
    Optional<CourseAssessmentOutput> findCourseAssessmentById(UUID courseAssessmentId);

    CourseAssessmentListOutput findCourseAssessmentsByCourseId(UUID courseId);

    @Transactional
    CourseAssessmentOutput createCourseAssessment(CreateCourseAssessmentInput input);

    @Transactional
    CourseAssessmentOutput updateCourseAssessment(UpdateCourseAssessmentInput input);

    @Transactional
    void deleteCourseAssessment(UUID courseAssessmentId);
}