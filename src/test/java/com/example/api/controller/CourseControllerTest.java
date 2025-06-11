package com.example.api.controller;

import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.course.CreateCourseRequest;
import com.example.api.controller.dto.course.UpdateCourseGradesRequest;
import com.example.api.controller.dto.course.UpdateCourseRequest;
import com.example.api.entity.CourseWeaknessAnalysis;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.CourseService;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.course.*;
import com.example.api.service.dto.semester.SemesterOutput;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@Import({TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CourseService courseService;

    @MockBean
    private SemesterService semesterService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private UUID semesterId;
    private UUID courseId;
    private SemesterOutput testSemesterOutput;
    private CourseOutput testCourseOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        // 직접 SemesterOutput 생성
        testSemesterOutput = new SemesterOutput();
        testSemesterOutput.setId(semesterId);
        testSemesterOutput.setUserId(userId);
        testSemesterOutput.setName("2025 봄학기");

        // 직접 CourseOutput 생성
        testCourseOutput = new CourseOutput();
        testCourseOutput.setId(courseId);
        testCourseOutput.setUserId(userId);
        testCourseOutput.setSemesterId(semesterId);
        testCourseOutput.setName("운영체제");
        testCourseOutput.setTargetGrade(4.0f);
        testCourseOutput.setEarnedGrade(0.0f);
        testCourseOutput.setCompletedCredits(3);
        testCourseOutput.setCreatedAt(LocalDateTime.now());
        testCourseOutput.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("학기별 과목 목록 조회")
    @WithMockUser
    void getCoursesBySemester() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        when(courseService.findCoursesBySemesterId(semesterId))
                .thenReturn(new CourseListOutput(List.of(testCourseOutput)));

        // When/Then
        mockMvc.perform(get("/v1/courses/semester/{semesterId}", semesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(1)))
                .andExpect(jsonPath("$.courses[0].id", is(courseId.toString())))
                .andExpect(jsonPath("$.courses[0].name", is("운영체제")));

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService).findCoursesBySemesterId(semesterId);
    }

    @Test
    @DisplayName("존재하지 않는 학기로 과목 조회")
    @WithMockUser
    void getCoursesBySemester_SemesterNotFound() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/v1/courses/semester/{semesterId}", semesterId))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService, never()).findCoursesBySemesterId(any());
    }

    @Test
    @DisplayName("다른 사용자의 학기로 과목 조회")
    @WithMockUser
    void getCoursesBySemester_Forbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        SemesterOutput otherSemesterOutput = new SemesterOutput();
        otherSemesterOutput.setId(semesterId);
        otherSemesterOutput.setUserId(otherUserId);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(otherSemesterOutput));

        // When/Then
        mockMvc.perform(get("/v1/courses/semester/{semesterId}", semesterId))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService, never()).findCoursesBySemesterId(any());
    }

    @Test
    @DisplayName("ID로 과목 조회")
    @WithMockUser
    void getCourseById() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));

        // When/Then
        mockMvc.perform(get("/v1/courses/{id}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(courseId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.semesterId", is(semesterId.toString())))
                .andExpect(jsonPath("$.name", is("운영체제")));

        verify(courseService).findCourseById(courseId);
    }

    @Test
    @DisplayName("ID로 과목 조회 시 찾을 수 없음")
    @WithMockUser
    void getCourseById_NotFound() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/v1/courses/{id}", courseId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
    }

    @Test
    @DisplayName("ID로 과목 조회 시 권한 없음")
    @WithMockUser
    void getCourseById_Forbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(otherUserId); // Different user ID
        forbiddenCourseOutput.setSemesterId(semesterId);
        forbiddenCourseOutput.setName("권한없는 과목");

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(forbiddenCourseOutput));

        // When/Then
        mockMvc.perform(get("/v1/courses/{id}", courseId))
                .andExpect(status().isForbidden());

        verify(courseService).findCourseById(courseId);
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 분석 데이터 있음")
    @WithMockUser
    void getCourseWeaknessAnalysis_WithData() throws Exception {
        // Given
        CourseWeaknessAnalysis weaknessAnalysis = new CourseWeaknessAnalysis();
        weaknessAnalysis.setWeaknesses("SQL 기본 문법에 대한 이해가 부족합니다. 특히 SELECT 문의 사용법과 WHERE 절의 개념이 명확하지 않습니다.");
        weaknessAnalysis.setSuggestions("SQL 기본 문법을 체계적으로 학습하고, 간단한 쿼리부터 차근차근 연습해보세요.");
        weaknessAnalysis.setAnalyzedAt(LocalDateTime.of(2024, 6, 11, 15, 30, 45));

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseService.findCourseWeaknessAnalysis(courseId))
                .thenReturn(weaknessAnalysis);

        // When/Then
        mockMvc.perform(get("/v1/courses/{courseId}/weakness-analysis", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weaknesses", is("SQL 기본 문법에 대한 이해가 부족합니다. 특히 SELECT 문의 사용법과 WHERE 절의 개념이 명확하지 않습니다.")))
                .andExpect(jsonPath("$.suggestions", is("SQL 기본 문법을 체계적으로 학습하고, 간단한 쿼리부터 차근차근 연습해보세요.")))
                .andExpect(jsonPath("$.analyzed_at", is("2024-06-11T15:30:45")));

        verify(courseService).findCourseById(courseId);
        verify(courseService).findCourseWeaknessAnalysis(courseId);
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 분석 데이터 없음")
    @WithMockUser
    void getCourseWeaknessAnalysis_NoData() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseService.findCourseWeaknessAnalysis(courseId))
                .thenReturn(null); // 분석 데이터 없음

        // When/Then
        mockMvc.perform(get("/v1/courses/{courseId}/weakness-analysis", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weaknesses").doesNotExist()) // null 값은 JSON에서 제외됨
                .andExpect(jsonPath("$.suggestions").doesNotExist())
                .andExpect(jsonPath("$.analyzed_at").doesNotExist());

        verify(courseService).findCourseById(courseId);
        verify(courseService).findCourseWeaknessAnalysis(courseId);
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 과목 찾을 수 없음")
    @WithMockUser
    void getCourseWeaknessAnalysis_CourseNotFound() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/v1/courses/{courseId}/weakness-analysis", courseId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        verify(courseService, never()).findCourseWeaknessAnalysis(any());
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 권한 없음")
    @WithMockUser
    void getCourseWeaknessAnalysis_Forbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(otherUserId);
        forbiddenCourseOutput.setSemesterId(semesterId);
        forbiddenCourseOutput.setName("권한없는 과목");

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(forbiddenCourseOutput));

        // When/Then
        mockMvc.perform(get("/v1/courses/{courseId}/weakness-analysis", courseId))
                .andExpect(status().isForbidden());

        verify(courseService).findCourseById(courseId);
        verify(courseService, never()).findCourseWeaknessAnalysis(any());
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 서버 에러")
    @WithMockUser
    void getCourseWeaknessAnalysis_ServerError() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseService.findCourseWeaknessAnalysis(courseId))
                .thenThrow(new RuntimeException("Database connection error"));

        // When/Then
        mockMvc.perform(get("/v1/courses/{courseId}/weakness-analysis", courseId))
                .andExpect(status().isInternalServerError());

        verify(courseService).findCourseById(courseId);
        verify(courseService).findCourseWeaknessAnalysis(courseId);
    }

    @Test
    @DisplayName("새 과목 생성")
    @WithMockUser
    void createCourse() throws Exception {
        // Given
        CreateCourseRequest createRequest = new CreateCourseRequest();
        createRequest.setSemesterId(semesterId);
        createRequest.setName("운영체제");

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        when(courseService.createCourse(any(CreateCourseInput.class)))
                .thenReturn(testCourseOutput);

        // When/Then
        mockMvc.perform(post("/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(courseId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.semesterId", is(semesterId.toString())))
                .andExpect(jsonPath("$.name", is("운영체제")));

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService).createCourse(argThat(input ->
                input.getUserId().equals(userId) &&
                        input.getSemesterId().equals(semesterId) &&
                        input.getName().equals("운영체제")
        ));
    }

    @Test
    @DisplayName("중복된 과목 생성 시 실패")
    @WithMockUser
    void createCourse_DuplicateCourse() throws Exception {
        // Given
        CreateCourseRequest createRequest = new CreateCourseRequest();
        createRequest.setSemesterId(semesterId);
        createRequest.setName("운영체제");

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemesterOutput));
        when(courseService.createCourse(any(CreateCourseInput.class)))
                .thenThrow(new IllegalArgumentException("Course with the same name already exists in this semester"));

        // When/Then
        mockMvc.perform(post("/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService).createCourse(any(CreateCourseInput.class));
    }

    @Test
    @DisplayName("존재하지 않는 학기에 과목 생성")
    @WithMockUser
    void createCourse_SemesterNotFound() throws Exception {
        // Given
        CreateCourseRequest createRequest = new CreateCourseRequest();
        createRequest.setSemesterId(semesterId);
        createRequest.setName("운영체제");

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(post("/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService, never()).createCourse(any(CreateCourseInput.class));
    }

    @Test
    @DisplayName("과목 정보 업데이트")
    @WithMockUser
    void updateCourse() throws Exception {
        // Given
        UpdateCourseRequest updateRequest = new UpdateCourseRequest();
        updateRequest.setName("고급 운영체제");

        CourseOutput updatedCourseOutput = new CourseOutput();
        updatedCourseOutput.setId(courseId);
        updatedCourseOutput.setUserId(userId);
        updatedCourseOutput.setSemesterId(semesterId);
        updatedCourseOutput.setName("고급 운영체제");
        updatedCourseOutput.setTargetGrade(testCourseOutput.getTargetGrade());
        updatedCourseOutput.setEarnedGrade(testCourseOutput.getEarnedGrade());
        updatedCourseOutput.setCompletedCredits(testCourseOutput.getCompletedCredits());
        updatedCourseOutput.setCreatedAt(testCourseOutput.getCreatedAt());
        updatedCourseOutput.setUpdatedAt(LocalDateTime.now());

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseService.updateCourse(any(UpdateCourseInput.class)))
                .thenReturn(updatedCourseOutput);

        // When/Then
        mockMvc.perform(put("/v1/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(courseId.toString())))
                .andExpect(jsonPath("$.name", is("고급 운영체제")));

        verify(courseService).findCourseById(courseId);
        verify(courseService).updateCourse(argThat(input ->
                input.getId().equals(courseId) &&
                        input.getName().equals("고급 운영체제")
        ));
    }

    @Test
    @DisplayName("존재하지 않는 과목 업데이트")
    @WithMockUser
    void updateCourse_NotFound() throws Exception {
        // Given
        UpdateCourseRequest updateRequest = new UpdateCourseRequest();
        updateRequest.setName("고급 운영체제");

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(put("/v1/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        verify(courseService, never()).updateCourse(any(UpdateCourseInput.class));
    }

    @Test
    @DisplayName("다른 사용자의 과목 업데이트")
    @WithMockUser
    void updateCourse_Forbidden() throws Exception {
        // Given
        UpdateCourseRequest updateRequest = new UpdateCourseRequest();
        updateRequest.setName("고급 운영체제");

        UUID otherUserId = UUID.randomUUID();
        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(otherUserId); // Different user ID
        forbiddenCourseOutput.setSemesterId(semesterId);
        forbiddenCourseOutput.setName("운영체제");

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(forbiddenCourseOutput));

        // When/Then
        mockMvc.perform(put("/v1/courses/{id}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(courseService).findCourseById(courseId);
        verify(courseService, never()).updateCourse(any(UpdateCourseInput.class));
    }

    @Test
    @DisplayName("과목 성적 정보 업데이트")
    @WithMockUser
    void updateCourseGrades() throws Exception {
        // Given
        UpdateCourseGradesRequest updateGradesRequest = new UpdateCourseGradesRequest();
        updateGradesRequest.setTargetGrade(4.3f);
        updateGradesRequest.setEarnedGrade(3.8f);
        updateGradesRequest.setCompletedCredits(3);

        CourseOutput updatedCourseOutput = new CourseOutput();
        updatedCourseOutput.setId(courseId);
        updatedCourseOutput.setUserId(userId);
        updatedCourseOutput.setSemesterId(semesterId);
        updatedCourseOutput.setName(testCourseOutput.getName());
        updatedCourseOutput.setTargetGrade(4.3f);
        updatedCourseOutput.setEarnedGrade(3.8f);
        updatedCourseOutput.setCompletedCredits(3);
        updatedCourseOutput.setCreatedAt(testCourseOutput.getCreatedAt());
        updatedCourseOutput.setUpdatedAt(LocalDateTime.now());

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseService.updateCourseGrades(any(UpdateCourseGradesInput.class)))
                .thenReturn(updatedCourseOutput);

        // When/Then
        mockMvc.perform(put("/v1/courses/{id}/grades", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateGradesRequest)))
                .andExpect(status().isNoContent());

        verify(courseService).findCourseById(courseId);
        verify(courseService).updateCourseGrades(argThat(input ->
                input.getId().equals(courseId) &&
                        input.getTargetGrade() == 4.3f &&
                        input.getEarnedGrade() == 3.8f &&
                        input.getCompletedCredits() == 3
        ));
    }

    @Test
    @DisplayName("일부 성적 정보만 업데이트")
    @WithMockUser
        void updateCourseGrades_PartialUpdate() throws Exception {
                // Given
                UpdateCourseGradesRequest updateGradesRequest = new UpdateCourseGradesRequest();
                updateGradesRequest.setEarnedGrade(3.8f); // Target grade는 업데이트하지 않음
                updateGradesRequest.setCompletedCredits(3);
        
                CourseOutput updatedCourseOutput = new CourseOutput();
                updatedCourseOutput.setId(courseId);
                updatedCourseOutput.setUserId(userId);
                updatedCourseOutput.setSemesterId(semesterId);
                updatedCourseOutput.setName(testCourseOutput.getName());
                updatedCourseOutput.setTargetGrade(testCourseOutput.getTargetGrade()); // 기존 값 유지
                updatedCourseOutput.setEarnedGrade(3.8f);
                updatedCourseOutput.setCompletedCredits(3);
                updatedCourseOutput.setCreatedAt(testCourseOutput.getCreatedAt());
                updatedCourseOutput.setUpdatedAt(LocalDateTime.now());
        
                when(courseService.findCourseById(courseId))
                        .thenReturn(Optional.of(testCourseOutput));
                when(courseService.updateCourseGrades(any(UpdateCourseGradesInput.class)))
                        .thenReturn(updatedCourseOutput);
                // When/Then
                mockMvc.perform(put("/v1/courses/{id}/grades", courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateGradesRequest)))
                        .andExpect(status().isNoContent());
        verify(courseService).findCourseById(courseId);
        verify(courseService).updateCourseGrades(argThat(input ->
                input.getId().equals(courseId) &&
                        input.getTargetGrade() == null && // Target grade는 업데이트하지 않음
                        input.getEarnedGrade() == 3.8f &&
                        input.getCompletedCredits() == 3
        ));
    }

    @Test
    @DisplayName("유효하지 않은 성적 정보로 업데이트")
    @WithMockUser
    void updateCourseGrades_InvalidGrades() throws Exception {
        // Given
        UpdateCourseGradesRequest updateGradesRequest = new UpdateCourseGradesRequest();
        updateGradesRequest.setTargetGrade(5.0f); // 4.5 초과 (컨트롤러에서 지정된 최대값)
        updateGradesRequest.setEarnedGrade(3.8f);
        updateGradesRequest.setCompletedCredits(3);

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        // When/Then
        mockMvc.perform(put("/v1/courses/{id}/grades", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateGradesRequest)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).updateCourseGrades(any(UpdateCourseGradesInput.class));
    }

    @Test
    @DisplayName("과목 삭제")
    @WithMockUser
    void deleteCourse() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        doNothing().when(courseService).deleteCourse(courseId);

        // When/Then
        mockMvc.perform(delete("/v1/courses/{id}", courseId))
                .andExpect(status().isNoContent());

        verify(courseService).findCourseById(courseId);
        verify(courseService).deleteCourse(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 과목 삭제")
    @WithMockUser
    void deleteCourse_NotFound() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(delete("/v1/courses/{id}", courseId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        verify(courseService, never()).deleteCourse(any());
    }

    @Test
    @DisplayName("다른 사용자의 과목 삭제")
    @WithMockUser
    void deleteCourse_Forbidden() throws Exception {
        // Given
        UUID otherUserId = UUID.randomUUID();
        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(otherUserId); // Different user ID
        forbiddenCourseOutput.setSemesterId(semesterId);
        forbiddenCourseOutput.setName("운영체제");

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(forbiddenCourseOutput));

        // When/Then
        mockMvc.perform(delete("/v1/courses/{id}", courseId))
                .andExpect(status().isForbidden());

        verify(courseService).findCourseById(courseId);
        verify(courseService, never()).deleteCourse(any());
    }
}
