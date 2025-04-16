package com.example.api.repository;

import com.example.api.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {

}
