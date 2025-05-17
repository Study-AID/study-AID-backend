package com.example.api.repository;

import com.example.api.entity.QuizItem;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


public interface QuizItemRepository extends JpaRepository<QuizItem, UUID> {
    List<QuizItem> findByQuizId(UUID quizId);
}
