package com.example.api.repository;

import org.springframework.stereotype.Repository;

import com.example.api.entity.QuizItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class QuizItemRepositoryImpl implements QuizItemRepositoryCustom {
    @PersistenceContext
    private EntityManager manager;

    @Transactional
    public QuizItem updateQuizItem(QuizItem quizItem) {
        try {
            return manager.merge(quizItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update QuizItem", e);
        }
    }
}
