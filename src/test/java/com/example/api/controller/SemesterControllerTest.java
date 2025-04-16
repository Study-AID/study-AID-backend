package com.example.api.controller;

import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.Season;
import com.example.api.service.SemesterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SemesterController.class)
@ActiveProfiles("test")
class SemesterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SemesterService semesterService;

    private UUID userId;
    private UUID semesterId;
    private User testUser;
    private Semester testSemester;

    @BeforeEach
    void setUp() {
        // TODO(mj): use @WithMockUser or @WithSecurityContext instead of hard-coding userId
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();

        // 테스트 사용자 설정
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        // 테스트 학기 설정
        testSemester = new Semester();
        testSemester.setId(semesterId);
        testSemester.setUser(testUser);
        testSemester.setName("2025 봄학기");
        testSemester.setYear(2025);
        testSemester.setSeason(Season.spring);
        testSemester.setStartDate(LocalDate.of(2025, 3, 2));
        testSemester.setEndDate(LocalDate.of(2025, 6, 30));
        testSemester.setTargetGrade(4.0f);
        testSemester.setEarnedGrade(0.0f);
        testSemester.setCompletedCredits(0);
        testSemester.setCreatedAt(LocalDateTime.now());
        testSemester.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("모든 학기 목록 조회")
    void getSemesters() throws Exception {
        // Given
        when(semesterService.findSemestersByUserId(userId))
                .thenReturn(Arrays.asList(testSemester));

        // When/Then
        mockMvc.perform(get("/v1/semesters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesters", hasSize(1)))
                .andExpect(jsonPath("$.semesters[0].id", is(semesterId.toString())))
                .andExpect(jsonPath("$.semesters[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.semesters[0].name", is("2025 봄학기")))
                .andExpect(jsonPath("$.semesters[0].year", is(2025)))
                .andExpect(jsonPath("$.semesters[0].season", is("spring")));

        verify(semesterService).findSemestersByUserId(userId);
    }

    @Test
    @DisplayName("학기가 없을 때 빈 목록 반환")
    void getSemesters_WhenNoSemesters_ReturnsEmptyList() throws Exception {
        // Given
        when(semesterService.findSemestersByUserId(userId))
                .thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/v1/semesters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesters", hasSize(0)));

        verify(semesterService).findSemestersByUserId(userId);
    }

    @Test
    @DisplayName("연도와 학기로 학기 조회")
    void getSemesterByYearAndSeason() throws Exception {
        // Given
        when(semesterService.findSemesterByUserAndYearAndSeason(eq(userId), eq(2025), eq(Season.spring)))
                .thenReturn(Optional.of(testSemester));

        // When/Then
        mockMvc.perform(get("/v1/semesters/search")
                        .param("year", "2025")
                        .param("season", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(semesterId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.name", is("2025 봄학기")))
                .andExpect(jsonPath("$.year", is(2025)))
                .andExpect(jsonPath("$.season", is("spring")));

        verify(semesterService).findSemesterByUserAndYearAndSeason(userId, 2025, Season.spring);
    }

    @Test
    @DisplayName("연도와 학기로 학기 조회 시 찾을 수 없음")
    void getSemesterByYearAndSeason_NotFound() throws Exception {
        // Given
        when(semesterService.findSemesterByUserAndYearAndSeason(eq(userId), eq(2025), eq(Season.spring)))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/v1/semesters/search")
                        .param("year", "2025")
                        .param("season", "spring"))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterByUserAndYearAndSeason(userId, 2025, Season.spring);
    }

    @Test
    @DisplayName("유효하지 않은 연도로 학기 조회")
    void getSemesterByYearAndSeason_InvalidYear() throws Exception {
        // When/Then
        mockMvc.perform(get("/v1/semesters/search")
                        .param("year", "-1")
                        .param("season", "spring"))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).findSemesterByUserAndYearAndSeason(any(), anyInt(), any());
    }

    @Test
    @DisplayName("유효하지 않은 학기로 학기 조회")
    void getSemesterByYearAndSeason_InvalidSeason() throws Exception {
        // When/Then
        mockMvc.perform(get("/v1/semesters/search")
                        .param("year", "2025")
                        .param("season", "invalid_season"))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).findSemesterByUserAndYearAndSeason(any(), anyInt(), any());
    }

    @Test
    @DisplayName("ID로 학기 조회")
    void getSemesterById() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));

        // When/Then
        mockMvc.perform(get("/v1/semesters/{id}", semesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(semesterId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.name", is("2025 봄학기")))
                .andExpect(jsonPath("$.year", is(2025)))
                .andExpect(jsonPath("$.season", is("spring")));

        verify(semesterService).findSemesterById(semesterId);
    }

    @Test
    @DisplayName("ID로 학기 조회 시 찾을 수 없음")
    void getSemesterById_NotFound() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/v1/semesters/{id}", semesterId))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterById(semesterId);
    }

    @Test
    @DisplayName("ID로 학기 조회 시 권한 없음")
    void getSemesterById_Forbidden() throws Exception {
        // Given
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Semester forbiddenSemester = new Semester();
        forbiddenSemester.setId(semesterId);
        forbiddenSemester.setUser(otherUser);
        forbiddenSemester.setYear(2025);
        forbiddenSemester.setSeason(Season.spring);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(forbiddenSemester));

        // When/Then
        mockMvc.perform(get("/v1/semesters/{id}", semesterId))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
    }

    @Test
    @DisplayName("새 학기 생성")
    void createSemester() throws Exception {
        // Given
        SemesterController.SemesterCreateDto createDto = new SemesterController.SemesterCreateDto();
        createDto.setName("2025 봄학기");
        createDto.setYear(2025);
        createDto.setSeason(Season.spring);

        when(semesterService.createSemester(eq(userId), eq("2025 봄학기"), eq(2025), eq(Season.spring)))
                .thenReturn(testSemester);

        // When/Then
        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(semesterId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.name", is("2025 봄학기")))
                .andExpect(jsonPath("$.year", is(2025)))
                .andExpect(jsonPath("$.season", is("spring")));

        verify(semesterService).createSemester(userId, "2025 봄학기", 2025, Season.spring);
    }

    @Test
    @DisplayName("중복된 학기 생성 시 실패")
    void createSemester_DuplicateSemester() throws Exception {
        // Given
        SemesterController.SemesterCreateDto createDto = new SemesterController.SemesterCreateDto();
        createDto.setName("2025 봄학기");
        createDto.setYear(2025);
        createDto.setSeason(Season.spring);

        when(semesterService.createSemester(eq(userId), eq("2025 봄학기"), eq(2025), eq(Season.spring)))
                .thenThrow(new IllegalArgumentException("Semester with the same year and season already exists"));

        // When/Then
        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());

        verify(semesterService).createSemester(userId, "2025 봄학기", 2025, Season.spring);
    }

    @Test
    @DisplayName("유효하지 않은 연도로 학기 생성")
    void createSemester_InvalidYear() throws Exception {
        // Given
        SemesterController.SemesterCreateDto createDto = new SemesterController.SemesterCreateDto();
        createDto.setName("Invalid Year 학기");
        createDto.setYear(-1);
        createDto.setSeason(Season.spring);

        // When/Then
        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).createSemester(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("학기 정보 업데이트")
    void updateSemester() throws Exception {
        // Given
        SemesterController.SemesterUpdateDto updateDto = new SemesterController.SemesterUpdateDto();
        updateDto.setName("2025 수정된 봄학기");
        updateDto.setYear(2025);
        updateDto.setSeason(Season.spring);

        Semester updatedSemester = new Semester();
        updatedSemester.setId(semesterId);
        updatedSemester.setUser(testUser);
        updatedSemester.setName("2025 수정된 봄학기");
        updatedSemester.setYear(2025);
        updatedSemester.setSeason(Season.spring);
        updatedSemester.setStartDate(testSemester.getStartDate());
        updatedSemester.setEndDate(testSemester.getEndDate());
        updatedSemester.setCreatedAt(testSemester.getCreatedAt());
        updatedSemester.setUpdatedAt(LocalDateTime.now());

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
        when(semesterService.updateSemester(
                eq(semesterId), eq("2025 수정된 봄학기"), eq(2025), eq(Season.spring)
        )).thenReturn(updatedSemester);

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(semesterId.toString())))
                .andExpect(jsonPath("$.name", is("2025 수정된 봄학기")));

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).updateSemester(semesterId, "2025 수정된 봄학기", 2025, Season.spring);
    }

    @Test
    @DisplayName("존재하지 않는 학기 업데이트")
    void updateSemester_NotFound() throws Exception {
        // Given
        SemesterController.SemesterUpdateDto updateDto = new SemesterController.SemesterUpdateDto();
        updateDto.setName("2025 수정된 봄학기");
        updateDto.setYear(2025);
        updateDto.setSeason(Season.spring);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).updateSemester(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("다른 사용자의 학기 업데이트")
    void updateSemester_Forbidden() throws Exception {
        // Given
        SemesterController.SemesterUpdateDto updateDto = new SemesterController.SemesterUpdateDto();
        updateDto.setName("2025 수정된 봄학기");
        updateDto.setYear(2025);
        updateDto.setSeason(Season.spring);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Semester forbiddenSemester = new Semester();
        forbiddenSemester.setId(semesterId);
        forbiddenSemester.setUser(otherUser);
        forbiddenSemester.setYear(2025);
        forbiddenSemester.setSeason(Season.spring);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(forbiddenSemester));

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).updateSemester(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("학기 날짜 업데이트")
    void updateSemesterDates() throws Exception {
        // Given
        SemesterController.SemesterUpdateDatesDto datesDto = new SemesterController.SemesterUpdateDatesDto();
        datesDto.setStartDate(LocalDate.of(2025, 3, 3));
        datesDto.setEndDate(LocalDate.of(2025, 7, 1));

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
        doNothing().when(semesterService).updateSemesterDates(
                eq(semesterId),
                eq(LocalDate.of(2025, 3, 3)),
                eq(LocalDate.of(2025, 7, 1))
        );

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/dates", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datesDto)))
                .andExpect(status().isNoContent());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).updateSemesterDates(
                semesterId,
                LocalDate.of(2025, 3, 3),
                LocalDate.of(2025, 7, 1)
        );
    }

    @Test
    @DisplayName("유효하지 않은 날짜로 학기 날짜 업데이트")
    void updateSemesterDates_InvalidDates() throws Exception {
        // Given
        SemesterController.SemesterUpdateDatesDto datesDto = new SemesterController.SemesterUpdateDatesDto();
        datesDto.setStartDate(LocalDate.of(2025, 8, 1));
        datesDto.setEndDate(LocalDate.of(2025, 3, 1)); // 종료일이 시작일보다 빠름

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/dates", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datesDto)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).updateSemesterDates(any(), any(), any());
    }

    @Test
    @DisplayName("학기 성적 정보 업데이트")
    void updateSemesterGrades() throws Exception {
        // Given
        SemesterController.SemesterUpdateGradesDto gradesDto = new SemesterController.SemesterUpdateGradesDto();
        gradesDto.setTargetGrade(4.3f);
        gradesDto.setEarnedGrade(3.8f);
        gradesDto.setCompletedCredits(18);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
        doNothing().when(semesterService).updateSemesterGrades(
                eq(semesterId),
                eq(4.3f),
                eq(3.8f),
                eq(18)
        );

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/grades", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesDto)))
                .andExpect(status().isNoContent());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).updateSemesterGrades(semesterId, 4.3f, 3.8f, 18);
    }

    @Test
    @DisplayName("유효하지 않은 성적 정보로 업데이트")
    void updateSemesterGrades_InvalidGrades() throws Exception {
        // Given
        SemesterController.SemesterUpdateGradesDto gradesDto = new SemesterController.SemesterUpdateGradesDto();
        gradesDto.setTargetGrade(5.0f); // 4.5 초과 (컨트롤러에서 지정된 최대값)
        gradesDto.setEarnedGrade(3.8f);
        gradesDto.setCompletedCredits(18);

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/grades", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesDto)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).updateSemesterGrades(any(), anyFloat(), anyFloat(), anyInt());
    }

    @Test
    @DisplayName("학기 삭제")
    void deleteSemester() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
        doNothing().when(semesterService).deleteSemester(semesterId);

        // When/Then
        mockMvc.perform(delete("/v1/semesters/{id}", semesterId))
                .andExpect(status().isNoContent());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).deleteSemester(semesterId);
    }

    @Test
    @DisplayName("존재하지 않는 학기 삭제")
    void deleteSemester_NotFound() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(delete("/v1/semesters/{id}", semesterId))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).deleteSemester(any());
    }

    @Test
    @DisplayName("다른 사용자의 학기 삭제")
    void deleteSemester_Forbidden() throws Exception {
        // Given
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Semester forbiddenSemester = new Semester();
        forbiddenSemester.setId(semesterId);
        forbiddenSemester.setUser(otherUser);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(forbiddenSemester));

        // When/Then
        mockMvc.perform(delete("/v1/semesters/{id}", semesterId))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).deleteSemester(any());
    }
}