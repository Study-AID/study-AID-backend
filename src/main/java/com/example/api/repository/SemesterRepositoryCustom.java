package com.example.api.repository;

import com.example.api.entity.Semester;
import com.example.api.entity.enums.Season;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepositoryCustom {
    List<Semester> findByUserId(UUID userId);

    Optional<Semester> findByUserIdAndYearAndSeason(
            UUID userId, int year, Season season
    );

    Semester createSemester(Semester semester);

    Semester updateSemester(Semester semester);

    void deleteSemester(UUID semesterId);
}
