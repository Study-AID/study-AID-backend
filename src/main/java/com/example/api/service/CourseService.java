package com.example.api.service;

import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.course.CreateCourseInput;
import com.example.api.service.dto.course.UpdateCourseGradesInput;
import com.example.api.service.dto.course.UpdateCourseInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface CourseService {
    Optional<CourseOutput> findCourseById(UUID courseId);

    List<CourseOutput> findCoursesByUserId(UUID userId);

    List<CourseOutput> findCoursesBySemesterId(UUID semesterId);

    @Transactional
    CourseOutput createCourse(CreateCourseInput input);

    @Transactional
    CourseOutput updateCourse(UpdateCourseInput input);

    @Transactional
    void updateCourseGrades(UpdateCourseGradesInput input);

    @Transactional
    void deleteCourse(UUID courseId);
}