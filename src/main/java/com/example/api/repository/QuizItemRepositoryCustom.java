package com.example.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.api.entity.QuizItem;

public interface QuizItemRepositoryCustom {
    Optional<QuizItem> findById(UUID id);

    List<QuizItem> findByQuizId(UUID quizId);

    QuizItem updateQuizItem(QuizItem quizItem);
}
