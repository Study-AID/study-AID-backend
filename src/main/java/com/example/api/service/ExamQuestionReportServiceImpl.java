package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.entity.Exam;
import com.example.api.entity.ExamItem;
import com.example.api.entity.ExamQuestionReport;
import com.example.api.entity.User;
import com.example.api.repository.ExamQuestionReportRepository;
import com.example.api.repository.ExamRepository;
import com.example.api.repository.ExamItemRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.report.CreateExamQuestionReportInput;
import com.example.api.service.dto.report.ExamQuestionReportOutput;

import jakarta.transaction.Transactional;

@Service
public class ExamQuestionReportServiceImpl implements ExamQuestionReportService {
    private final ExamQuestionReportRepository examQuestionReportRepo;
    private final UserRepository userRepo;
    private final ExamRepository examRepo;
    private final ExamItemRepository examItemRepo;

    @Autowired
    public ExamQuestionReportServiceImpl(
            ExamQuestionReportRepository examQuestionReportRepo,
            UserRepository userRepo,
            ExamRepository examRepo,
            ExamItemRepository examItemRepo
    ) {
        this.examQuestionReportRepo = examQuestionReportRepo;
        this.userRepo = userRepo;
        this.examRepo = examRepo;
        this.examItemRepo = examItemRepo;
    }

    @Override
    public Optional<ExamQuestionReportOutput> findReportById(UUID reportId) {
        return examQuestionReportRepo.findById(reportId)
                .map(ExamQuestionReportOutput::fromEntity);
    }

    @Override
    @Transactional
    public ExamQuestionReportOutput createReport(CreateExamQuestionReportInput input) {
        // Validate that user, exam, and examItem exist
        User user = userRepo.findById(input.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Exam exam = examRepo.findById(input.getExamId())
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        ExamItem examItem = examItemRepo.findById(input.getExamItemId())
                .orElseThrow(() -> new IllegalArgumentException("Exam item not found"));

        if (input.getReportReason() == null || input.getReportReason().isEmpty()) {
            throw new IllegalArgumentException("Report reason cannot be null or empty");
        }

        ExamQuestionReport report = new ExamQuestionReport();
        report.setUser(user);
        report.setExam(exam);
        report.setExamItem(examItem);
        report.setReportReason(input.getReportReason());

        ExamQuestionReport createdReport = examQuestionReportRepo.createExamQuestionReport(report);
        return ExamQuestionReportOutput.fromEntity(createdReport);
    }

    @Override
    public List<ExamQuestionReportOutput> findReportsByUser(UUID userId) {
        List<ExamQuestionReport> reports = examQuestionReportRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream().map(ExamQuestionReportOutput::fromEntity).toList();
    }

    @Override
    @Transactional
    public Boolean deleteReport(UUID reportId) {
        Optional<ExamQuestionReport> reportOpt = examQuestionReportRepo.findById(reportId);
        if (reportOpt.isEmpty()) {
            return false;
        }
        examQuestionReportRepo.deleteById(reportId);
        return true;
    }
}
