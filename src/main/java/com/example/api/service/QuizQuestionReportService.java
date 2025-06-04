package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.service.dto.report.CreateQuizQuestionReportInput;
import com.example.api.service.dto.report.QuizQuestionReportOutput;

import jakarta.transaction.Transactional;

@Service
public interface QuizQuestionReportService {
    Optional<QuizQuestionReportOutput> findReportById(UUID reportId);

    @Transactional
    QuizQuestionReportOutput createReport(CreateQuizQuestionReportInput input);

    List<QuizQuestionReportOutput> findReportsByUser(UUID userId);

    @Transactional
    Boolean deleteReport(UUID reportId);
}