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

    /**
     * 학기 ID로 학기를 조회합니다.
     *
     * @param semesterId 조회할 학기 ID
     * @return 학기 정보
     */
    public Optional<Semester> findSemesterById(UUID semesterId) {
        return semesterRepo.findById(semesterId);
    }

    /**
     * 사용자의 모든 학기를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 학기 목록
     */
    public List<Semester> findSemestersByUserId(UUID userId) {
        return semesterRepo.findByUserId(userId);
    }

    /**
     * 사용자, 연도, 학기로 특정 학기를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param year   연도
     * @param season 학기 (봄, 여름, 가을, 겨울)
     * @return 학기 정보
     */
    public Optional<Semester> findSemesterByUserAndYearAndSeason(
            UUID userId, int year, Season season
    ) {
        return semesterRepo.findByUserIdAndYearAndSeason(userId, year, season);
    }

    /**
     * 학기를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param name   학기명
     * @param year   연도
     * @param season 학기
     * @return 생성된 학기
     * @throws IllegalArgumentException 같은 연도와 학기의 학기가 이미 존재하는 경우
     */
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

    /**
     * 학기 기본 정보를 업데이트합니다.
     *
     * @param id     업데이트할 학기 ID
     * @param name   학기명
     * @param year   연도
     * @param season 학기
     * @return 업데이트된 학기
     */
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

    /**
     * 학기 날짜 정보를 업데이트합니다.
     *
     * @param id        업데이트할 학기 ID
     * @param startDate 시작일
     * @param endDate   종료일
     */
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

    /**
     * 학기 성적 정보를 업데이트합니다.
     *
     * @param id               업데이트할 학기 ID
     * @param targetGrade      목표 성적
     * @param earnedGrade      취득 성적
     * @param completedCredits 이수 학점
     */
    @Transactional
    public void updateSemesterGrades(
            UUID id, float targetGrade, float earnedGrade, int completedCredits
    ) {
        Semester semester = new Semester();
        semester.setId(id);
        semester.setTargetGrade(targetGrade);
        semester.setEarnedGrade(earnedGrade);
        semester.setCompletedCredits(completedCredits);
        semesterRepo.updateSemester(semester);
    }

    /**
     * 학기를 삭제합니다.
     *
     * @param semesterId 삭제할 학기 ID
     */
    @Transactional
    public void deleteSemester(UUID semesterId) {
        semesterRepo.deleteSemester(semesterId);
    }

    private String defaultSemesterName(int year, Season season) {
        return year + "-" + season.name();
    }
}