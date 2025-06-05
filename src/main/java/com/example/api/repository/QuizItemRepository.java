package com.example.api.repository;

import com.example.api.entity.QuizItem;

import java.util.List;
import java.util.UUID;

import com.example.api.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuizItemRepository extends JpaRepository<QuizItem, UUID>, QuizItemRepositoryCustom {
    List<QuizItem> findByQuizId(UUID quizId);
    
    boolean existsByQuizIdAndQuestionTypeAndDeletedAtIsNull(UUID quizId, QuestionType questionType);

    QuizItem updateQuizItem(QuizItem quizItem);
}
