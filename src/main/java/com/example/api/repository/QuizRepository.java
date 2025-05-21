package com.example.api.repository;

import com.example.api.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID>, QuizRepositoryCustom {
    List<Quiz> findByLectureId(UUID lectureId);

    List<Quiz> findByUserId(UUID userId);

    Quiz createQuiz(Quiz quiz);

    Quiz updateQuiz(Quiz quiz);

    void deleteQuiz(UUID quizId);
}
