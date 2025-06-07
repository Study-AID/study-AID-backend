package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import com.example.api.entity.CourseAssessment;

public interface CourseAssessmentRepositoryCustom {
    List<CourseAssessment> findByCourseId(UUID courseId);

    CourseAssessment createCourseAssessment(CourseAssessment courseAssessment);

    CourseAssessment updateCourseAssessment(CourseAssessment courseAssessment);

    void deleteCourseAssessment(UUID courseAssessmentId);
}