package com.example.api.repository;

import com.example.api.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

}
