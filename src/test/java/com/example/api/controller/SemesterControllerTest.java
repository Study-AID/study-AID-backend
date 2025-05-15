package com.example.api.controller;

import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.semester.CreateSemesterRequest;
import com.example.api.controller.dto.semester.UpdateSemesterDatesRequest;
import com.example.api.controller.dto.semester.UpdateSemesterGradesRequest;
import com.example.api.controller.dto.semester.UpdateSemesterRequest;
import com.example.api.entity.enums.Season;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.semester.*;
import com.example.api.util.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SemesterController.class)
@Import({TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SemesterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SemesterService semesterService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private UUID semesterId;
    private SemesterOutput testSemesterOutput;
    private SemesterListOutput testSemesterListOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();

        testSemesterOutput = new SemesterOutput(
                semesterId,
                userId,
                "2025 봄학기",
                2025,
                Season.spring,
                LocalDate.of(2025, 3, 2),
                LocalDate.of(2025, 6, 30),
                4.0f,
                0.0f,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        testSemesterListOutput = new SemesterListOutput(
                Arrays.asList(testSemesterOutput)
        );
    }

    @Test
    @DisplayName("모든 학기 목록 조회")
    @WithMockUser
    void getSemesters() throws Exception {
        // Given
        when(semesterService.findSemestersByUserId(userId))
                .thenReturn(testSemesterListOutput);

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
    @WithMockUser
    void getSemesters_WhenNoSemesters_ReturnsEmptyList() throws Exception {
        // Given
        when(semesterService.findSemestersByUserId(userId))
                .thenReturn(new SemesterListOutput(Collections.emptyList()));

        // When/Then
        mockMvc.perform(get("/v1/semesters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesters", nullValue()));

        verify(semesterService).findSemestersByUserId(userId);
    }

    @Test
    @DisplayName("연도와 학기로 학기 조회")
    @WithMockUser
    void getSemesterByYearAndSeason() throws Exception {
        // Given
        when(semesterService.findSemesterByUserAndYearAndSeason(eq(userId), eq(2025), eq(Season.spring)))
                .thenReturn(Optional.of(testSemesterOutput));

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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
    void getSemesterById() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));

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
    @WithMockUser
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
    @WithMockUser
    void getSemesterById_Forbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        SemesterOutput forbiddenSemesterOutput = new SemesterOutput(
                semesterId,
                otherUserId,
                "2025 봄학기",
                2025,
                Season.spring,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(forbiddenSemesterOutput));

        // When/Then
        mockMvc.perform(get("/v1/semesters/{id}", semesterId))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
    }

    @Test
    @DisplayName("새 학기 생성")
    @WithMockUser
    void createSemester() throws Exception {
        // Given
        CreateSemesterRequest createRequest = new CreateSemesterRequest(
                "2025 봄학기",
                2025,
                "spring"
        );

        when(semesterService.createSemester(any(CreateSemesterInput.class)))
                .thenReturn(testSemesterOutput);

        // When/Then
        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(semesterId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.name", is("2025 봄학기")))
                .andExpect(jsonPath("$.year", is(2025)))
                .andExpect(jsonPath("$.season", is("spring")));

        verify(semesterService).createSemester(any(CreateSemesterInput.class));
    }

    @Test
    @DisplayName("중복된 학기 생성 시 실패")
    @WithMockUser
    void createSemester_DuplicateSemester() throws Exception {
        // Given
        CreateSemesterRequest createRequest = new CreateSemesterRequest(
                "2025 봄학기",
                2025,
                "spring"
        );

        when(semesterService.createSemester(any(CreateSemesterInput.class)))
                .thenThrow(new IllegalArgumentException("Semester with the same year and season already exists"));

        // When/Then
        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(semesterService).createSemester(any(CreateSemesterInput.class));
    }

    @Test
    @DisplayName("유효하지 않은 연도로 학기 생성")
    @WithMockUser
    void createSemester_InvalidYear() throws Exception {
        // Given
        CreateSemesterRequest createRequest = new CreateSemesterRequest(
                "Invalid Year 학기",
                -1,
                "spring"
        );

        // When/Then
        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).createSemester(any(CreateSemesterInput.class));
    }

    @Test
    @DisplayName("학기 정보 업데이트")
    @WithMockUser
    void updateSemester() throws Exception {
        // Given
        UpdateSemesterRequest updateRequest = new UpdateSemesterRequest(
                "2025 수정된 봄학기",
                2025,
                "spring"
        );

        SemesterOutput updatedSemesterOutput = new SemesterOutput(
                semesterId,
                userId,
                "2025 수정된 봄학기",
                2025,
                Season.spring,
                testSemesterOutput.getStartDate(),
                testSemesterOutput.getEndDate(),
                testSemesterOutput.getTargetGrade(),
                testSemesterOutput.getEarnedGrade(),
                testSemesterOutput.getCompletedCredits(),
                testSemesterOutput.getCreatedAt(),
                LocalDateTime.now()
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        when(semesterService.updateSemester(any(UpdateSemesterInput.class)))
                .thenReturn(updatedSemesterOutput);

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(semesterId.toString())))
                .andExpect(jsonPath("$.name", is("2025 수정된 봄학기")));

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).updateSemester(any(UpdateSemesterInput.class));
    }

    @Test
    @DisplayName("존재하지 않는 학기 업데이트")
    @WithMockUser
    void updateSemester_NotFound() throws Exception {
        // Given
        UpdateSemesterRequest updateRequest = new UpdateSemesterRequest(
                "2025 수정된 봄학기",
                2025,
                "spring"
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).updateSemester(any(UpdateSemesterInput.class));
    }

    @Test
    @DisplayName("다른 사용자의 학기 업데이트")
    @WithMockUser
    void updateSemester_Forbidden() throws Exception {
        // Given
        UpdateSemesterRequest updateRequest = new UpdateSemesterRequest(
                "2025 수정된 봄학기",
                2025,
                "spring"
        );

        UUID otherUserId = UUID.randomUUID();
        SemesterOutput forbiddenSemesterOutput = new SemesterOutput(
                semesterId,
                otherUserId,
                "2025 봄학기",
                2025,
                Season.spring,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(forbiddenSemesterOutput));

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).updateSemester(any(UpdateSemesterInput.class));
    }

    @Test
    @DisplayName("학기 날짜 업데이트")
    @WithMockUser
    void updateSemesterDates() throws Exception {
        // Given
        UpdateSemesterDatesRequest datesRequest = new UpdateSemesterDatesRequest(
                LocalDate.of(2025, 3, 3),
                LocalDate.of(2025, 7, 1)
        );

        SemesterOutput updatedSemesterOutput = new SemesterOutput(
                semesterId,
                userId,
                testSemesterOutput.getName(),
                testSemesterOutput.getYear(),
                testSemesterOutput.getSeason(),
                LocalDate.of(2025, 3, 3),
                LocalDate.of(2025, 7, 1),
                testSemesterOutput.getTargetGrade(),
                testSemesterOutput.getEarnedGrade(),
                testSemesterOutput.getCompletedCredits(),
                testSemesterOutput.getCreatedAt(),
                LocalDateTime.now()
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        when(semesterService.updateSemesterDates(any(UpdateSemesterDatesInput.class)))
                .thenReturn(updatedSemesterOutput);

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/dates", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datesRequest)))
                .andExpect(status().isOk());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).updateSemesterDates(any(UpdateSemesterDatesInput.class));
    }

    @Test
    @DisplayName("유효하지 않은 날짜로 학기 날짜 업데이트")
    @WithMockUser
    void updateSemesterDates_InvalidDates() throws Exception {
        // Given
        UpdateSemesterDatesRequest datesRequest = new UpdateSemesterDatesRequest(
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 3, 1) // 종료일이 시작일보다 빠름
        );

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/dates", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datesRequest)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).updateSemesterDates(any(UpdateSemesterDatesInput.class));
    }

    @Test
    @DisplayName("학기 성적 정보 업데이트")
    @WithMockUser
    void updateSemesterGrades() throws Exception {
        // Given
        UpdateSemesterGradesRequest gradesRequest = new UpdateSemesterGradesRequest(
                4.3f,
                3.8f,
                18
        );

        SemesterOutput updatedSemesterOutput = new SemesterOutput(
                semesterId,
                userId,
                testSemesterOutput.getName(),
                testSemesterOutput.getYear(),
                testSemesterOutput.getSeason(),
                testSemesterOutput.getStartDate(),
                testSemesterOutput.getEndDate(),
                4.3f,
                3.8f,
                18,
                testSemesterOutput.getCreatedAt(),
                LocalDateTime.now()
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        when(semesterService.updateSemesterGrades(any(UpdateSemesterGradesInput.class)))
                .thenReturn(updatedSemesterOutput);

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/grades", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesRequest)))
                .andExpect(status().isOk());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).updateSemesterGrades(any(UpdateSemesterGradesInput.class));
    }

    @Test
    @DisplayName("유효하지 않은 성적 정보로 업데이트")
    @WithMockUser
    void updateSemesterGrades_InvalidGrades() throws Exception {
        // Given
        UpdateSemesterGradesRequest gradesRequest = new UpdateSemesterGradesRequest(
                5.0f, // 4.5 초과 (컨트롤러에서 지정된 최대값)
                3.8f,
                18
        );

        // When/Then
        mockMvc.perform(put("/v1/semesters/{id}/grades", semesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesRequest)))
                .andExpect(status().isBadRequest());

        verify(semesterService, never()).updateSemesterGrades(any(UpdateSemesterGradesInput.class));
    }

    @Test
    @DisplayName("학기 삭제")
    @WithMockUser
    void deleteSemester() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        doNothing().when(semesterService).deleteSemester(semesterId);

        // When/Then
        mockMvc.perform(delete("/v1/semesters/{id}", semesterId))
                .andExpect(status().isNoContent());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService).deleteSemester(semesterId);
    }

    @Test
    @DisplayName("존재하지 않는 학기 삭제")
    @WithMockUser
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
    @WithMockUser
    void deleteSemester_Forbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        SemesterOutput forbiddenSemesterOutput = new SemesterOutput(
                semesterId,
                otherUserId,
                "2025 봄학기",
                2025,
                Season.spring,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(forbiddenSemesterOutput));

        // When/Then
        mockMvc.perform(delete("/v1/semesters/{id}", semesterId))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
        verify(semesterService, never()).deleteSemester(any());
    }
}
