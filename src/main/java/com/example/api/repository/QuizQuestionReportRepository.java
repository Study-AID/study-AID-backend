package com.example.api.repository;

import com.example.api.entity.QuizQuestionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizQuestionReportRepository extends JpaRepository<QuizQuestionReport, UUID> {

}
