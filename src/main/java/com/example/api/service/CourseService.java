package com.example.api.service;

import com.example.api.entity.CourseWeaknessAnalysis;
import com.example.api.service.dto.course.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public interface CourseService {
    Optional<CourseOutput> findCourseById(UUID courseId);

    CourseWeaknessAnalysis findCourseWeaknessAnalysis(UUID courseId);

    CourseListOutput findCoursesBySemesterId(UUID semesterId);

    @Transactional
    CourseOutput createCourse(CreateCourseInput input);

    @Transactional
    CourseOutput updateCourse(UpdateCourseInput input);

    @Transactional
    CourseOutput updateCourseGrades(UpdateCourseGradesInput input);

    @Transactional
    void deleteCourse(UUID courseId);
}
