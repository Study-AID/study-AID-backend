package com.example.api.service;

import com.example.api.entity.enums.Season;
import com.example.api.service.dto.semester.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public interface SemesterService {
    Optional<SemesterOutput> findSemesterById(UUID semesterId);

    SemesterListOutput findSemestersByUserId(UUID userId);

    Optional<SemesterOutput> findSemesterByUserAndYearAndSeason(UUID userId, int year, Season season);

    @Transactional
    SemesterOutput createSemester(CreateSemesterInput input);

    @Transactional
    SemesterOutput updateSemester(UpdateSemesterInput input);

    @Transactional
    SemesterOutput updateSemesterDates(UpdateSemesterDatesInput input);

    @Transactional
    SemesterOutput updateSemesterGrades(UpdateSemesterGradesInput input);

    void deleteSemester(UUID semesterId);
}
