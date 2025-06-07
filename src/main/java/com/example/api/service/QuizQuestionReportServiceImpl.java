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
        User user = userRepo.getReferenceById(input.getUserId());
        Quiz quiz = quizRepo.getReferenceById(input.getQuizId());
        QuizItem quizItem = quizItemRepo.getReferenceById(input.getQuizItemId());

        // Validate that user, quiz, and quizItem exist
        if (user == null || quiz == null || quizItem == null || input.getReportReason() == null) {
            throw new IllegalArgumentException("Missing required fields for report creation");
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