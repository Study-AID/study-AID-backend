package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.QuizItem;

public interface QuizItemRepository extends JpaRepository<QuizItem, UUID> {

}
