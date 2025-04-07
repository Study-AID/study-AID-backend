package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.CourseAssessment;

public interface CourseAssessmentRepository extends JpaRepository<CourseAssessment, UUID> {

}
