package com.example.api.service;

import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.Season;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class SemesterServiceImpl implements SemesterService {
    private UserRepository userRepo;
    private SemesterRepository semesterRepo;

    @Autowired
    public void SemesterService(UserRepository userRepo, SemesterRepository semesterRepo) {
        this.userRepo = userRepo;
        this.semesterRepo = semesterRepo;
    }

    public Optional<Semester> findSemesterById(UUID semesterId) {
        return semesterRepo.findById(semesterId);
    }


    public List<Semester> findSemestersByUserId(UUID userId) {
        return semesterRepo.findByUserId(userId);
    }


    public Optional<Semester> findSemesterByUserAndYearAndSeason(UUID userId, int year, Season season) {
        return semesterRepo.findByUserIdAndYearAndSeason(userId, year, season);
    }

    @Transactional
    public Semester createSemester(UUID userId, String name, int year, Season season) {
        User user = userRepo.getReferenceById(userId);

        Semester semester = new Semester();
        semester.setUser(user);
        if (Objects.equals(name, "")) {
            name = defaultSemesterName(year, season);
        }
        semester.setName(name);
        semester.setYear(year);
        semester.setSeason(season);
        return semesterRepo.createSemester(semester);
    }

    @Transactional
    public Semester updateSemester(UUID id, String name, int year, Season season) {
        Semester semester = new Semester();
        semester.setId(id);
        if (Objects.equals(name, "")) {
            name = defaultSemesterName(year, season);
        }
        semester.setName(name);
        semester.setYear(year);
        semester.setSeason(season);
        return semesterRepo.updateSemester(semester);
    }

    @Transactional
    public void updateSemesterDates(UUID id, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Semester semester = new Semester();
        semester.setId(id);
        semester.setStartDate(startDate);
        semester.setEndDate(endDate);
        semesterRepo.updateSemester(semester);
    }

    @Transactional
    public void updateSemesterGrades(UUID id, float targetGrade, float earnedGrade, int completedCredits) {
        Semester semester = new Semester();
        semester.setId(id);
        semester.setTargetGrade(targetGrade);
        semester.setEarnedGrade(earnedGrade);
        semester.setCompletedCredits(completedCredits);
        semesterRepo.updateSemester(semester);
    }

    @Transactional
    public void deleteSemester(UUID semesterId) {
        semesterRepo.deleteSemester(semesterId);
    }

    private String defaultSemesterName(int year, Season season) {
        return year + "-" + season.name();
    }
}