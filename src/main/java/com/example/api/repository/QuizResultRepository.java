package com.example.api.repository;

import com.example.api.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {

}
