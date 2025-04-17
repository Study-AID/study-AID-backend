package com.example.api.repository;

import com.example.api.entity.CourseAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseAssessmentRepository extends JpaRepository<CourseAssessment, UUID> {

}
