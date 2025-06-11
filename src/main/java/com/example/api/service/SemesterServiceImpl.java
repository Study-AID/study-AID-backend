package com.example.api.service;

import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.Season;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.semester.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public Optional<SemesterOutput> findSemesterById(UUID semesterId) {
        Optional<Semester> semester = semesterRepo.findById(semesterId);
        return semester.map(SemesterOutput::fromEntity);
    }

    @Override
    public SemesterListOutput findSemestersByUserId(UUID userId) {
        List<Semester> semesters = semesterRepo.findByUserId(userId);
        return SemesterListOutput.fromEntities(semesters);
    }

    @Override
    public Optional<SemesterOutput> findSemesterByUserAndYearAndSeason(UUID userId, int year, Season season) {
        Optional<Semester> semester = semesterRepo.findByUserIdAndYearAndSeason(userId, year, season);
        return semester.map(SemesterOutput::fromEntity);
    }

    @Override
    @Transactional
    public SemesterOutput createSemester(CreateSemesterInput input) {
        User user = userRepo.getReferenceById(input.getUserId());

        Semester semester = new Semester();
        semester.setUser(user);
        String name = input.getName();
        if (Objects.equals(name, "")) {
            name = defaultSemesterName(input.getYear(), input.getSeason());
        }
        semester.setName(name);
        semester.setYear(input.getYear());
        semester.setSeason(input.getSeason());

        Semester createdSemester = semesterRepo.createSemester(semester);
        return SemesterOutput.fromEntity(createdSemester);
    }

    @Override
    @Transactional
    public SemesterOutput updateSemester(UpdateSemesterInput input) {
        // 기존 Entity 조회 후 필드 업데이트 (new Entity 방식에서 get and set 방식으로 변경)
        Semester semester = semesterRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Semester not found: " + input.getId()));
        String name = input.getName();
        if (Objects.equals(name, "")) {
            name = defaultSemesterName(input.getYear(), input.getSeason());
        }
        semester.setName(name);
        semester.setYear(input.getYear());
        semester.setSeason(input.getSeason());

        Semester updatedSemester = semesterRepo.updateSemester(semester);
        return SemesterOutput.fromEntity(updatedSemester);
    }

    @Override
    @Transactional
    public SemesterOutput updateSemesterDates(UpdateSemesterDatesInput input) {
        if (input.getEndDate().isBefore(input.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // 기존 Entity 조회 후 필드 업데이트 (new Entity 방식에서 get and set 방식으로 변경)
        Semester semester = semesterRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Semester not found: " + input.getId()));
        semester.setStartDate(input.getStartDate());
        semester.setEndDate(input.getEndDate());

        Semester updatedSemester = semesterRepo.updateSemester(semester);
        return SemesterOutput.fromEntity(updatedSemester);
    }

    @Override
    @Transactional
    public SemesterOutput updateSemesterGrades(UpdateSemesterGradesInput input) {
        // 기존 Entity 조회 후 필드 업데이트 (new Entity 방식에서 get and set 방식으로 변경)
        Semester semester = semesterRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Semester not found: " + input.getId()));
        semester.setTargetGrade(input.getTargetGrade());
        semester.setEarnedGrade(input.getEarnedGrade());
        semester.setCompletedCredits(input.getCompletedCredits());

        Semester updatedSemester = semesterRepo.updateSemester(semester);
        return SemesterOutput.fromEntity(updatedSemester);
    }

    @Override
    @Transactional
    public void deleteSemester(UUID semesterId) {
        semesterRepo.deleteSemester(semesterId);
    }

    private String defaultSemesterName(int year, Season season) {
        return year + "-" + season.name();
    }
}
