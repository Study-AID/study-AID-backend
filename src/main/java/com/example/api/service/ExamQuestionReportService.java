package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.service.dto.report.CreateExamQuestionReportInput;
import com.example.api.service.dto.report.ExamQuestionReportOutput;

import jakarta.transaction.Transactional;

@Service
public interface ExamQuestionReportService {
    Optional<ExamQuestionReportOutput> findReportById(UUID reportId);
    
    @Transactional
    ExamQuestionReportOutput createReport(CreateExamQuestionReportInput input);

    List<ExamQuestionReportOutput> findReportsByUser(UUID userId);

    @Transactional
    Boolean deleteReport(UUID reportId);
}
