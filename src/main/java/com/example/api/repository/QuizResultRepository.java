package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.QuizResult;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {

}
