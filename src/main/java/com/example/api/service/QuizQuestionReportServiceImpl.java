package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.entity.Quiz;
import com.example.api.entity.QuizItem;
import com.example.api.entity.QuizQuestionReport;
import com.example.api.entity.User;
import com.example.api.repository.QuizQuestionReportRepository;
import com.example.api.repository.QuizRepository;
import com.example.api.repository.QuizItemRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.report.CreateQuizQuestionReportInput;
import com.example.api.service.dto.report.QuizQuestionReportOutput;

import jakarta.transaction.Transactional;

@Service
public class QuizQuestionReportServiceImpl implements QuizQuestionReportService {
    private QuizQuestionReportRepository quizQuestionReportRepo;
    private UserRepository userRepo;
    private QuizRepository quizRepo;
    private QuizItemRepository quizItemRepo;

    @Autowired
    public QuizQuestionReportServiceImpl(
            QuizQuestionReportRepository quizQuestionReportRepo,
            UserRepository userRepo,
            QuizRepository quizRepo,
            QuizItemRepository quizItemRepo
    ) {
        this.quizQuestionReportRepo = quizQuestionReportRepo;
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.quizItemRepo = quizItemRepo;
    }

    @Override
    public Optional<QuizQuestionReportOutput> findReportById(UUID reportId) {
        return quizQuestionReportRepo.findById(reportId)
                .map(QuizQuestionReportOutput::fromEntity);
    }

    @Override
    @Transactional
    public QuizQuestionReportOutput createReport(CreateQuizQuestionReportInput input) {
        // Validate that user, quiz, and quizItem exist
        User user = userRepo.findById(input.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Quiz quiz = quizRepo.findById(input.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        QuizItem quizItem = quizItemRepo.findById(input.getQuizItemId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz item not found"));

        if (input.getReportReason() == null || input.getReportReason().isEmpty()) {
            throw new IllegalArgumentException("Report reason cannot be empty");
        }
        
        QuizQuestionReport report = new QuizQuestionReport();
        report.setUser(user);
        report.setQuiz(quiz);
        report.setQuizItem(quizItem);
        report.setReportReason(input.getReportReason());

        QuizQuestionReport createdReport = quizQuestionReportRepo.createQuizQuestionReport(report);
        return QuizQuestionReportOutput.fromEntity(createdReport);
    }

    @Override
    public List<QuizQuestionReportOutput> findReportsByUser(UUID userId) {
        List<QuizQuestionReport> reports = quizQuestionReportRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
                .map(QuizQuestionReportOutput::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public Boolean deleteReport(UUID reportId) {
        Optional<QuizQuestionReport> reportOpt = quizQuestionReportRepo.findById(reportId);
        if (reportOpt.isEmpty()) {
            return false;
        }
        quizQuestionReportRepo.deleteById(reportId);
        return true;
    }
}