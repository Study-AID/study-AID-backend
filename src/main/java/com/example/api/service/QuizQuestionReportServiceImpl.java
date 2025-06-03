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
import com.example.api.service.dto.quiz.CreateQuizQuestionReportInput;
import com.example.api.service.dto.quiz.QuizQuestionReportOutput;

import jakarta.transaction.Transactional;

@Service
public class QuizQuestionReportServiceImpl implements QuizQuestionReportService {
    private QuizQuestionReportRepository quizQuestionReportRepo;
    private UserRepository userRepo;
    private QuizRepository quizRepo;
    private QuizItemRepository quizItemRepo;

    @Autowired
    public void QuizQuestionReportService(
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
    @Transactional
    public QuizQuestionReportOutput createReport(CreateQuizQuestionReportInput input) {
        User user = userRepo.getReferenceById(input.getUserId());
        Quiz quiz = quizRepo.getReferenceById(input.getQuizId());
        QuizItem quizItem = quizItemRepo.getReferenceById(input.getQuizItemId());

        QuizQuestionReport report = new QuizQuestionReport();
        report.setUser(user);
        report.setQuiz(quiz);
        report.setQuizItem(quizItem);
        report.setReportReason(input.getReportReason());

        QuizQuestionReport createdReport = quizQuestionReportRepo.createQuizQuestionReport(report);
        return QuizQuestionReportOutput.fromEntity(createdReport);
    }

    @Override
    public List<QuizQuestionReportOutput> getReportsByQuizItem(UUID quizItemId) {
        List<QuizQuestionReport> reports = quizQuestionReportRepo.findByQuizItemIdOrderByCreatedAtDesc(quizItemId);
        return reports.stream()
                .map(QuizQuestionReportOutput::fromEntity)
                .toList();
    }

    @Override
    public List<QuizQuestionReportOutput> getReportsByUser(UUID userId) {
        List<QuizQuestionReport> reports = quizQuestionReportRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
                .map(QuizQuestionReportOutput::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId, UUID userId) {
        Optional<QuizQuestionReport> reportOpt = quizQuestionReportRepo.findById(reportId);
        
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Report not found with id: " + reportId);
        }

        QuizQuestionReport report = reportOpt.get();
        
        if (!report.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this report");
        }

        if (report.getDeletedAt() != null) {
            throw new IllegalArgumentException("Report is already deleted");
        }

        quizQuestionReportRepo.deleteById(reportId);
    }
}