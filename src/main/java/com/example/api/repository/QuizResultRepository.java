package com.example.api.repository;

import com.example.api.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID>, QuizResultRepositoryCustom {
    List<QuizResult> findByLectureId(UUID lectureId);
    
    QuizResult createQuizResult(QuizResult quizResult);
}
