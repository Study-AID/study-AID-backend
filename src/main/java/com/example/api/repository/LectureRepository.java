package com.example.api.repository;

import com.example.api.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LectureRepository extends JpaRepository<Lecture, UUID> {

}
