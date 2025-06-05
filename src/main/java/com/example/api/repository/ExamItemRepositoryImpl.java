package com.example.api.repository;

import org.springframework.stereotype.Repository;

import com.example.api.entity.ExamItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class ExamItemRepositoryImpl {
    @PersistenceContext
    private EntityManager manager;
    
    @Transactional
    public ExamItem updateExamItem(ExamItem examItem) {
        try {
            return manager.merge(examItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ExamItem", e);
        }
    }
}
