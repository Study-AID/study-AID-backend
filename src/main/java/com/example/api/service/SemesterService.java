package com.example.api.service;

import com.example.api.entity.Semester;
import com.example.api.entity.enums.Season;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public interface SemesterService {
    Optional<Semester> findSemesterById(UUID semesterId);

    List<Semester> findSemestersByUserId(UUID userId);

    Optional<Semester> findSemesterByUserAndYearAndSeason(UUID userId, int year, Season season);

    @Transactional
    Semester createSemester(UUID userId, String name, int year, Season season);

    @Transactional
    Semester updateSemester(UUID id, String name, int year, Season season);

    @Transactional
    void updateSemesterDates(UUID id, LocalDate startDate, LocalDate endDate);

    @Transactional
    void updateSemesterGrades(UUID id, float targetGrade, float earnedGrade, int completedCredits);

    void deleteSemester(UUID semesterId);
}