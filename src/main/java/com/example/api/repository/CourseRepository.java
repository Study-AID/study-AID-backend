package com.example.api.repository;

import com.example.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository
        extends JpaRepository<Course, UUID>, CourseRepositoryCustom {
    List<Course> findBySemesterId(UUID semesterId);

    List<Course> findByUserId(UUID userId);

    Course createCourse(Course course);

    Course updateCourse(Course course);

    void deleteCourse(UUID courseId);
}
