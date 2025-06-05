package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.service.dto.quiz.*;

import jakarta.transaction.Transactional;

@Service
public interface QuizService {
    Optional<QuizOutput> findQuizById(UUID quizId);

    QuizListOutput findQuizzesByLectureId(UUID lectureId);

    @Transactional
    QuizOutput createQuiz(CreateQuizInput input);

    @Transactional
    QuizOutput updateQuiz(UpdateQuizInput input);

    @Transactional
    void deleteQuiz(UUID quizId);

    @Transactional
    QuizResponseListOutput submitAndGradeQuizWithStatus(List<CreateQuizResponseInput> inputs);
    
    @Transactional
    void gradeNonEssayQuestions(UUID quizId);

    @Transactional
    QuizResultOutput createQuizResult(CreateQuizResultInput input);

    @Transactional
    Optional<QuizResultOutput> findQuizResultByQuizId(UUID quizId);

    @Transactional
    QuizResultListOutput findQuizResultsByCourseId(UUID courseId);

    @Transactional
    Float calculateQuizAverageScore(UUID courseId);
}
