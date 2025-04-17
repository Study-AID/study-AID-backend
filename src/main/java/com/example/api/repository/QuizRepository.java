package com.example.api.repository;

import com.example.api.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

}
