package com.example.api.service;

import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.Season;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SemesterServiceTest {
    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SemesterServiceImpl semesterService;

    private UUID userId;
    private UUID semesterId;
    private User testUser;
    private Semester testSemester;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        semesterId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);

        testSemester = new Semester();
        testSemester.setId(semesterId);
        testSemester.setUser(testUser);
        testSemester.setYear(2025);
        testSemester.setSeason(Season.spring);
        testSemester.setName("2025 spring 학기");
    }

    @Test
    @DisplayName("ID로 학기 조회")
    void findSemesterById() {
        // Given
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(testSemester));

        // When
        Optional<Semester> semester = semesterService.findSemesterById(semesterId);

        // Then
        assertTrue(semester.isPresent());
        assertEquals(testSemester, semester.get());
        verify(semesterRepository).findById(semesterId);
    }

    @Test
    @DisplayName("사용자 ID로 학기 목록 조회")
    void findSemestersByUserId() {
        // Given
        List<Semester> expectedSemesters = Arrays.asList(testSemester);
        when(semesterRepository.findByUserId(userId)).thenReturn(expectedSemesters);

        // When
        List<Semester> actualSemesters = semesterService.findSemestersByUserId(userId);

        // Then
        assertThat(actualSemesters).isEqualTo(expectedSemesters);
        verify(semesterRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 ID, 연도, 학기로 학기 조회")
    void findSemesterByUserAndYearAndSeason() {
        // Given
        when(semesterRepository.findByUserIdAndYearAndSeason(userId, 2025, Season.spring))
                .thenReturn(Optional.of(testSemester));

        // When
        Optional<Semester> semester = semesterService.findSemesterByUserAndYearAndSeason(userId, 2025, Season.spring);

        // Then
        assertTrue(semester.isPresent());
        assertEquals(testSemester, semester.get());
        verify(semesterRepository).findByUserIdAndYearAndSeason(userId, 2025, Season.spring);
    }

    @Test
    @DisplayName("학기 생성")
    void createSemester() {
        // Givengst
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(semesterRepository.createSemester(any(Semester.class))).thenReturn(testSemester);

        // When
        Semester createdSemester = semesterService.createSemester(userId, "2025-1학기", 2025, Season.spring);

        // Then
        assertNotNull(createdSemester);
        assertEquals(testSemester, createdSemester);
        verify(userRepository).getReferenceById(userId);
        verify(semesterRepository).createSemester(any(Semester.class));
    }

    @Test
    @DisplayName("학기 생성 - 중복 학기 예외 발생")
    void createSemester_DuplicateSemester() {
        // Given
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(semesterRepository.createSemester(any(Semester.class)))
                .thenThrow(new InvalidDataAccessApiUsageException("Semester with the same year and season already exists"));

        // When/Then
        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            semesterService.createSemester(userId, "2025-1학기", 2025, Season.spring);
        });
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).getReferenceById(userId);
        verify(semesterRepository).createSemester(any(Semester.class));
    }

    @Test
    @DisplayName("학기 기본 정보 업데이트")
    void updateSemester() {
        // Given
        Semester expectedSemester = testSemester;
        expectedSemester.setName("2025-spring");
        when(semesterRepository.updateSemester(any(Semester.class))).
                thenReturn(expectedSemester);

        // When
        Semester updatedSemester = semesterService.updateSemester(
                expectedSemester.getId(), "", 2025, Season.spring
        );

        // Then
        assertEquals(expectedSemester, updatedSemester);
        verify(semesterRepository).updateSemester(argThat(semester ->
                semester.getId().equals(semesterId) &&
                        semester.getName().equals(expectedSemester.getName()) &&
                        semester.getYear() == expectedSemester.getYear() &&
                        semester.getSeason() == expectedSemester.getSeason()
        ));
    }

    @Test
    @DisplayName("학기 날짜 정보 업데이트")
    void updateSemesterDates() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 3, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        Semester expectedSemester = testSemester;
        expectedSemester.setStartDate(startDate);
        expectedSemester.setEndDate(endDate);
        when(semesterRepository.updateSemester(any(Semester.class))).
                thenReturn(testSemester);

        // When
        semesterService.updateSemesterDates(semesterId, startDate, endDate);

        // Then
        verify(semesterRepository).updateSemester(argThat(semester ->
                semester.getId().equals(semesterId) &&
                        semester.getStartDate().equals(startDate) &&
                        semester.getEndDate().equals(endDate)
        ));
    }

    @Test
    @DisplayName("학기 날짜 정보 업데이트 - 예외 발생")
    void updateSemesterDates_InvalidDate() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 3, 1);
        LocalDate endDate = LocalDate.of(2025, 2, 1);

        Semester expectedSemester = testSemester;
        expectedSemester.setStartDate(startDate);
        expectedSemester.setEndDate(endDate);

        // When/Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            semesterService.updateSemesterDates(semesterId, startDate, endDate);
        });
        assertTrue(exception.getMessage().contains("End date cannot be before start date"));
    }

    @Test
    @DisplayName("학기 성적 정보 업데이트")
    void updateSemesterGrades() {
        // Given
        float targetGrade = 4.0f;
        float earnedGrade = 3.5f;
        int completedCredits = 15;

        Semester expectedSemester = testSemester;
        expectedSemester.setTargetGrade(targetGrade);
        expectedSemester.setEarnedGrade(earnedGrade);
        expectedSemester.setCompletedCredits(completedCredits);
        when(semesterRepository.updateSemester(any(Semester.class))).
                thenReturn(expectedSemester);

        // When
        semesterService.updateSemesterGrades(semesterId, targetGrade, earnedGrade, completedCredits);

        // Then
        verify(semesterRepository).updateSemester(argThat(semester ->
                semester.getId().equals(semesterId) &&
                        semester.getTargetGrade() == targetGrade &&
                        semester.getEarnedGrade() == earnedGrade &&
                        semester.getCompletedCredits() == completedCredits
        ));
    }

    @Test
    @DisplayName("학기 삭제")
    void deleteSemester() {
        // Given
        doNothing().when(semesterRepository).deleteSemester(semesterId);

        // When
        semesterService.deleteSemester(semesterId);

        // Then
        verify(semesterRepository).deleteSemester(semesterId);
    }
}