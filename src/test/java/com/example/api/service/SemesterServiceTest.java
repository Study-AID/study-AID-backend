package com.example.api.service;

import com.example.api.entity.Lecture;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.Season;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.semester.*;
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
        Optional<SemesterOutput> semesterOutput = semesterService.findSemesterById(semesterId);

        // Then
        assertTrue(semesterOutput.isPresent());
        assertEquals(semesterId, semesterOutput.get().getId());
        assertEquals(userId, semesterOutput.get().getUserId());
        assertEquals("2025 spring 학기", semesterOutput.get().getName());
        assertEquals(2025, semesterOutput.get().getYear());
        assertEquals(Season.spring, semesterOutput.get().getSeason());
        verify(semesterRepository).findById(semesterId);
    }

    @Test
    @DisplayName("사용자 ID로 학기 목록 조회")
    void findSemestersByUserId() {
        // Given
        List<Semester> expectedSemesters = Arrays.asList(testSemester);
        when(semesterRepository.findByUserId(userId)).thenReturn(expectedSemesters);

        // When
        SemesterListOutput semesterListOutput = semesterService.findSemestersByUserId(userId);

        // Then
        assertNotNull(semesterListOutput);
        assertEquals(1, semesterListOutput.getSemesters().size());
        assertEquals(semesterId, semesterListOutput.getSemesters().get(0).getId());
        assertEquals(userId, semesterListOutput.getSemesters().get(0).getUserId());
        verify(semesterRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 ID, 연도, 학기로 학기 조회")
    void findSemesterByUserAndYearAndSeason() {
        // Given
        when(semesterRepository.findByUserIdAndYearAndSeason(userId, 2025, Season.spring))
                .thenReturn(Optional.of(testSemester));

        // When
        Optional<SemesterOutput> semesterOutput = semesterService.findSemesterByUserAndYearAndSeason(userId, 2025, Season.spring);

        // Then
        assertTrue(semesterOutput.isPresent());
        assertEquals(semesterId, semesterOutput.get().getId());
        assertEquals(userId, semesterOutput.get().getUserId());
        verify(semesterRepository).findByUserIdAndYearAndSeason(userId, 2025, Season.spring);
    }

    @Test
    @DisplayName("학기 생성")
    void createSemester() {
        // Given
        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(semesterRepository.createSemester(any(Semester.class))).thenReturn(testSemester);

        CreateSemesterInput input = new CreateSemesterInput();
        input.setUserId(userId);
        input.setName("2025-1학기");
        input.setYear(2025);
        input.setSeason(Season.spring);

        // When
        SemesterOutput createdSemester = semesterService.createSemester(input);

        // Then
        assertNotNull(createdSemester);
        assertEquals(semesterId, createdSemester.getId());
        assertEquals(userId, createdSemester.getUserId());
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

        CreateSemesterInput input = new CreateSemesterInput();
        input.setUserId(userId);
        input.setName("2025-1학기");
        input.setYear(2025);
        input.setSeason(Season.spring);

        // When/Then
        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            semesterService.createSemester(input);
        });
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).getReferenceById(userId);
        verify(semesterRepository).createSemester(any(Semester.class));
    }

    @Test
    @DisplayName("학기 기본 정보 업데이트")
    void updateSemester() {
        // Given
        UpdateSemesterInput input = new UpdateSemesterInput();
        input.setId(semesterId);
        input.setName("");
        input.setYear(2025);
        input.setSeason(Season.spring);

        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.updateSemester(any(Semester.class))).thenReturn(testSemester);

        // When
        SemesterOutput updatedSemester = semesterService.updateSemester(input);

        // Then
        assertEquals(semesterId, updatedSemester.getId());
        assertEquals("2025-spring", updatedSemester.getName());
        verify(semesterRepository, times(1)).findById(semesterId);
        verify(semesterRepository, times(1)).updateSemester(any(Semester.class));
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
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.updateSemester(any(Semester.class))).
                thenReturn(testSemester);

        UpdateSemesterDatesInput input = new UpdateSemesterDatesInput();
        input.setId(semesterId);
        input.setStartDate(startDate);
        input.setEndDate(endDate);

        // When
        SemesterOutput updatedSemester = semesterService.updateSemesterDates(input);

        // Then
        assertNotNull(updatedSemester);
        verify(semesterRepository, times(1)).findById(semesterId);
        verify(semesterRepository, times(1)).updateSemester(argThat(semester ->
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

        UpdateSemesterDatesInput input = new UpdateSemesterDatesInput();
        input.setId(semesterId);
        input.setStartDate(startDate);
        input.setEndDate(endDate);

        // When/Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            semesterService.updateSemesterDates(input);
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
        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(testSemester));
        when(semesterRepository.updateSemester(any(Semester.class))).
                thenReturn(expectedSemester);

        UpdateSemesterGradesInput input = new UpdateSemesterGradesInput();
        input.setId(semesterId);
        input.setTargetGrade(targetGrade);
        input.setEarnedGrade(earnedGrade);
        input.setCompletedCredits(completedCredits);

        // When
        SemesterOutput updatedSemester = semesterService.updateSemesterGrades(input);

        // Then
        assertNotNull(updatedSemester);
        assertEquals(targetGrade, updatedSemester.getTargetGrade());
        assertEquals(earnedGrade, updatedSemester.getEarnedGrade());
        assertEquals(completedCredits, updatedSemester.getCompletedCredits());
        verify(semesterRepository, times(1)).findById(semesterId);
        verify(semesterRepository, times(1)).updateSemester(argThat(semester ->
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
