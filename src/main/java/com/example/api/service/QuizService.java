package com.example.api.service;

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
    QuizResponseOutput createQuizResponse(CreateQuizResponseInput input);

    @Transactional
    void gradeQuiz(UUID quizId);
}
