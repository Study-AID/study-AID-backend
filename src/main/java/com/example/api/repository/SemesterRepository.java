package com.example.api.repository;

import com.example.api.entity.Semester;
import com.example.api.entity.enums.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepository
        extends JpaRepository<Semester, UUID>, SemesterRepositoryCustom {
    List<Semester> findByUserId(UUID userId);

    Optional<Semester> findByUserIdAndYearAndSeason(
            UUID user_id, int year, Season season
    );

    Semester createSemester(Semester semester);

    Semester updateSemester(Semester semester);

    void deleteSemester(UUID semesterId);
}
