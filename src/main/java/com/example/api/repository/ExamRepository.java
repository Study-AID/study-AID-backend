package com.example.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.Exam;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

}
