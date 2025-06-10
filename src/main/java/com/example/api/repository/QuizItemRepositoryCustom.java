package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.QuizItem;
import com.example.api.entity.enums.QuestionType;

public interface QuizItemRepositoryCustom {
    Optional<QuizItem> findById(UUID id);

    List<QuizItem> findByQuizId(UUID quizId);

    boolean existsByQuizIdAndQuestionTypeAndDeletedAtIsNull(UUID quizId, QuestionType questionType);

    QuizItem updateQuizItem(QuizItem quizItem);
}
