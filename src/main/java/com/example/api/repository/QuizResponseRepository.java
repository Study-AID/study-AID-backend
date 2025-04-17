package com.example.api.repository;

import com.example.api.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, UUID> {

}
