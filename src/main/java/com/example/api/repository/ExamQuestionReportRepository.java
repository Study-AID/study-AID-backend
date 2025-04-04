package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.ExamQuestionReport;

public interface ExamQuestionReportRepository extends JpaRepository<ExamQuestionReport, UUID> {

}
