package com.example.api.repository;


import java.util.List;
import java.util.UUID;

import com.example.api.entity.Quiz;

public interface QuizRepositoryCustom {
    List<Quiz> findByLectureId(UUID lectureId);

    List<Quiz> findByUserId(UUID userId);

    Quiz createQuiz(Quiz quiz);

    Quiz updateQuiz(Quiz quiz);

    void deleteQuiz(UUID quizId);
}
