
package com.example.api.repository;

import com.example.api.entity.CourseAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseAssessmentRepository extends JpaRepository<CourseAssessment, UUID>, CourseAssessmentRepositoryCustom {
    List<CourseAssessment> findByCourseId(UUID courseId);

    CourseAssessment createCourseAssessment(CourseAssessment courseAssessment);

    CourseAssessment updateCourseAssessment(CourseAssessment courseAssessment);

    void deleteCourseAssessment(UUID courseAssessmentId);
}