package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.QuizResult;

public interface QuizResultRepositoryCustom {
    Optional<QuizResult> findByQuizId(UUID quizId);
    
    List<QuizResult> findByLectureId(UUID lectureId);
    
    QuizResult createQuizResult(QuizResult quizResult);
}
