package com.example.api.repository;

import com.example.api.entity.ExamQuestionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamQuestionReportRepository extends JpaRepository<ExamQuestionReport, UUID> {

}
