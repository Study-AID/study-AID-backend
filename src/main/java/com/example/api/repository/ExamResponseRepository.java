package com.example.api.repository;

import com.example.api.entity.ExamResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamResponseRepository extends JpaRepository<ExamResponse, UUID> {

}
