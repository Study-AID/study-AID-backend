package com.example.api.repository;

import com.example.api.entity.CourseActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseActivityLogRepository extends JpaRepository<CourseActivityLog, UUID> {

}
