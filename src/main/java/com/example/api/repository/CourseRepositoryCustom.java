package com.example.api.repository;

import com.example.api.entity.Course;

import java.util.List;
import java.util.UUID;

public interface CourseRepositoryCustom {
    List<Course> findBySemesterId(UUID semesterId);

    Course createCourse(Course course);

    Course updateCourse(Course course);

    void deleteCourse(UUID courseId);
}