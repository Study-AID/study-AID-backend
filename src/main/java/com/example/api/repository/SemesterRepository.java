package com.example.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.entity.Semester;
import com.example.api.entity.User;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    List<User> findByName(String SemesterName);
}