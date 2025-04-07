package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.QuizQuestionReport;

public interface QuizQuestionReportRepository extends JpaRepository<QuizQuestionReport, UUID> {

}
