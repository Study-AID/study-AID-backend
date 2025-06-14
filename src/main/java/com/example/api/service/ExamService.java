package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.service.dto.exam.CreateExamInput;
import com.example.api.service.dto.exam.CreateExamResponseInput;
import com.example.api.service.dto.exam.ExamItemListOutput;
import com.example.api.service.dto.exam.ExamItemOutput;
import com.example.api.service.dto.exam.ExamListOutput;
import com.example.api.service.dto.exam.ExamOutput;
import com.example.api.service.dto.exam.ExamResponseListOutput;
import com.example.api.service.dto.exam.ExamResultListOutput;
import com.example.api.service.dto.exam.ExamResultOutput;
import com.example.api.service.dto.exam.ToggleLikeExamItemInput;
import com.example.api.service.dto.exam.UpdateExamInput;

@Service
public interface ExamService {
    Optional<ExamOutput> findExamById(UUID examId);
    
    ExamListOutput findExamsByCourseId(UUID courseId);

    @Transactional
    ExamOutput createExam(CreateExamInput input);

    @Transactional
    ExamOutput updateExam(UpdateExamInput input);

    @Transactional
    void deleteExam(UUID examId);

    @Transactional
    ExamResponseListOutput submitAndGradeExamWithStatus(List<CreateExamResponseInput> inputs);

    @Transactional
    void gradeNonEssayQuestions(UUID examId);

    @Transactional
    Optional<ExamResultOutput> findExamResultByExamId(UUID examId);

    @Transactional
    ExamResultListOutput findExamResultsByCourseId(UUID courseId);

    @Transactional
    Float calculateExamAverageScore(UUID courseId);

    ExamItemListOutput findLikedExamItemByCourseId(UUID courseId);

    @Transactional
    ExamItemOutput toggleLikeExamItem(ToggleLikeExamItemInput input);
}