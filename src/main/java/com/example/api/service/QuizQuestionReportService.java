package com.example.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.service.dto.quiz.CreateQuizQuestionReportInput;
import com.example.api.service.dto.quiz.QuizQuestionReportOutput;

import jakarta.transaction.Transactional;

@Service
public interface QuizQuestionReportService {
    @Transactional
    QuizQuestionReportOutput createReport(CreateQuizQuestionReportInput input);

    List<QuizQuestionReportOutput> getReportsByQuizItem(UUID quizItemId);

    List<QuizQuestionReportOutput> getReportsByUser(UUID userId);

    @Transactional
    void deleteReport(UUID reportId, UUID userId);
}