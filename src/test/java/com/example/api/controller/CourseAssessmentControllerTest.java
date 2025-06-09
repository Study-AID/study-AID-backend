package com.example.api.controller;

import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.courseAssessment.CreateCourseAssessmentRequest;
import com.example.api.controller.dto.courseAssessment.UpdateCourseAssessmentRequest;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.CourseAssessmentService;
import com.example.api.service.CourseService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.courseAssessment.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseAssessmentController.class)
@Import({TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CourseAssessmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CourseAssessmentService courseAssessmentService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private UUID courseId;
    private UUID courseAssessmentId;
    private CourseOutput testCourseOutput;
    private CourseAssessmentOutput testCourseAssessmentOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        courseId = UUID.randomUUID();
        courseAssessmentId = UUID.randomUUID();

        // create test CourseOutput
        testCourseOutput = new CourseOutput();
        testCourseOutput.setId(courseId);
        testCourseOutput.setUserId(userId);
        testCourseOutput.setName("운영체제");
        testCourseOutput.setCreatedAt(LocalDateTime.now());
        testCourseOutput.setUpdatedAt(LocalDateTime.now());

        // create test CourseAssessmentOutput
        testCourseAssessmentOutput = new CourseAssessmentOutput();
        testCourseAssessmentOutput.setId(courseAssessmentId);
        testCourseAssessmentOutput.setCourseId(courseId);
        testCourseAssessmentOutput.setUserId(userId);
        testCourseAssessmentOutput.setTitle("중간고사");
        testCourseAssessmentOutput.setScore(85.0f);
        testCourseAssessmentOutput.setMaxScore(100.0f);
        testCourseAssessmentOutput.setCreatedAt(LocalDateTime.now());
        testCourseAssessmentOutput.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("과목 내 과제 평가 목록 조회 테스트")
    @WithMockUser
    void getCourseAssessmentsByCourseTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.findCourseAssessmentsByCourseId(courseId))
                .thenReturn(new CourseAssessmentListOutput(List.of(testCourseAssessmentOutput)));

        // When & Then
        mockMvc.perform(get("/v1/courses/{courseId}/assessments", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseAssessments", hasSize(1)))
                .andExpect(jsonPath("$.courseAssessments[0].id").value(courseAssessmentId.toString()))
                .andExpect(jsonPath("$.courseAssessments[0].title").value("중간고사"))
                .andExpect(jsonPath("$.courseAssessments[0].score").value(85.0))
                .andExpect(jsonPath("$.courseAssessments[0].maxScore").value(100.0))
                .andExpect(jsonPath("$.courseAssessments[0].courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.courseAssessments[0].userId").value(userId.toString()));

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).findCourseAssessmentsByCourseId(courseId);
    }

    @Test
    @DisplayName("과제 평가 목록 조회 실패 테스트 - 존재하지 않는 과목")
    @WithMockUser
    void getCourseAssessmentsByCourseNotFoundTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/courses/{courseId}/assessments", courseId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService, never()).findCourseAssessmentsByCourseId(courseId);
    }

    @Test
    @DisplayName("과제 평가 목록 조회 실패 테스트 - 다른 사용자의 과목")
    @WithMockUser
    void getCourseAssessmentsByCourseIdForbiddenTest() throws Exception {
        // Given
        UUID otherUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        CourseOutput otherCourseOutput = new CourseOutput();
        otherCourseOutput.setId(courseId);
        otherCourseOutput.setUserId(otherUserId);
        otherCourseOutput.setName("운영체제");

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(otherCourseOutput));

        // When & Then
        mockMvc.perform(get("/v1/courses/{courseId}/assessments", courseId))
                .andExpect(status().isForbidden());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService, never()).findCourseAssessmentsByCourseId(courseId);
    }

    @Test
    @DisplayName("ID로 과제 평가 조회 테스트")
    @WithMockUser
    void getCourseAssessmentByIdTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.findCourseAssessmentById(courseAssessmentId))
                .thenReturn(Optional.of(testCourseAssessmentOutput));

        // When & Then
        mockMvc.perform(get("/v1/courses/{courseId}/assessments/{id}", courseId, courseAssessmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseAssessmentId.toString()))
                .andExpect(jsonPath("$.title").value("중간고사"))
                .andExpect(jsonPath("$.score").value(85.0))
                .andExpect(jsonPath("$.maxScore").value(100.0))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).findCourseAssessmentById(courseAssessmentId);
    }

    @Test
    @DisplayName("ID로 과제 평가 조회 실패 테스트 - 다른 과목의 평가")
    @WithMockUser
    void getCourseAssessmentByIdWrongCourseTest() throws Exception {
        // Given
        UUID otherCourseId = UUID.randomUUID();
        CourseAssessmentOutput otherCourseAssessment = new CourseAssessmentOutput();
        otherCourseAssessment.setId(courseAssessmentId);
        otherCourseAssessment.setCourseId(otherCourseId); // 다른 과목의 평가
        otherCourseAssessment.setUserId(userId);

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.findCourseAssessmentById(courseAssessmentId))
                .thenReturn(Optional.of(otherCourseAssessment));

        // When & Then
        mockMvc.perform(get("/v1/courses/{courseId}/assessments/{id}", courseId, courseAssessmentId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).findCourseAssessmentById(courseAssessmentId);
    }

    @Test
    @DisplayName("새 과제 평가 생성 테스트")
    @WithMockUser
    void createCourseAssessmentTest() throws Exception {
        // Given
        CreateCourseAssessmentRequest request = new CreateCourseAssessmentRequest();
        request.setTitle("기말고사");
        request.setScore(90.0f);
        request.setMaxScore(100.0f);

        CourseAssessmentOutput createdOutput = new CourseAssessmentOutput();
        createdOutput.setId(UUID.randomUUID());
        createdOutput.setCourseId(courseId);
        createdOutput.setUserId(userId);
        createdOutput.setTitle("기말고사");
        createdOutput.setScore(90.0f);
        createdOutput.setMaxScore(100.0f);
        createdOutput.setCreatedAt(LocalDateTime.now());
        createdOutput.setUpdatedAt(LocalDateTime.now());

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.createCourseAssessment(any(CreateCourseAssessmentInput.class)))
                .thenReturn(createdOutput);

        // When & Then
        mockMvc.perform(post("/v1/courses/{courseId}/assessments", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("기말고사"))
                .andExpect(jsonPath("$.score").value(90.0))
                .andExpect(jsonPath("$.maxScore").value(100.0))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).createCourseAssessment(argThat(input ->
                input.getCourseId().equals(courseId) &&
                input.getUserId().equals(userId) &&
                input.getTitle().equals("기말고사") &&
                input.getScore().equals(90.0f) &&
                input.getMaxScore().equals(100.0f)
        ));
    }

    @Test
    @DisplayName("과제 평가 생성 실패 테스트 - 점수가 만점보다 높음")
    @WithMockUser
    void createCourseAssessmentInvalidScoreTest() throws Exception {
        // Given
        CreateCourseAssessmentRequest request = new CreateCourseAssessmentRequest();
        request.setTitle("테스트");
        request.setScore(110.0f); // 만점보다 높은 점수
        request.setMaxScore(100.0f);

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));

        // When & Then
        mockMvc.perform(post("/v1/courses/{courseId}/assessments", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService, never()).createCourseAssessment(any());
    }

    @Test
    @DisplayName("과제 평가 생성 실패 테스트 - 빈 제목")
    @WithMockUser
    void createCourseAssessmentEmptyTitleTest() throws Exception {
        // Given
        CreateCourseAssessmentRequest request = new CreateCourseAssessmentRequest();
        request.setTitle(""); // 빈 제목
        request.setScore(90.0f);
        request.setMaxScore(100.0f);

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));

        // When & Then
        mockMvc.perform(post("/v1/courses/{courseId}/assessments", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService, never()).createCourseAssessment(any());
    }

    @Test
    @DisplayName("과제 평가 업데이트 테스트")
    @WithMockUser
    void updateCourseAssessmentTest() throws Exception {
        // Given
        UpdateCourseAssessmentRequest request = new UpdateCourseAssessmentRequest();
        request.setTitle("수정된 중간고사");
        request.setScore(88.0f);
        request.setMaxScore(100.0f);

        CourseAssessmentOutput updatedOutput = new CourseAssessmentOutput();
        updatedOutput.setId(courseAssessmentId);
        updatedOutput.setCourseId(courseId);
        updatedOutput.setUserId(userId);
        updatedOutput.setTitle("수정된 중간고사");
        updatedOutput.setScore(88.0f);
        updatedOutput.setMaxScore(100.0f);
        updatedOutput.setCreatedAt(LocalDateTime.now());
        updatedOutput.setUpdatedAt(LocalDateTime.now());

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.findCourseAssessmentById(courseAssessmentId))
                .thenReturn(Optional.of(testCourseAssessmentOutput));
        when(courseAssessmentService.updateCourseAssessment(any(UpdateCourseAssessmentInput.class)))
                .thenReturn(updatedOutput);

        // When & Then
        mockMvc.perform(put("/v1/courses/{courseId}/assessments/{id}", courseId, courseAssessmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseAssessmentId.toString()))
                .andExpect(jsonPath("$.title").value("수정된 중간고사"))
                .andExpect(jsonPath("$.score").value(88.0))
                .andExpect(jsonPath("$.maxScore").value(100.0));

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).findCourseAssessmentById(courseAssessmentId);
        verify(courseAssessmentService).updateCourseAssessment(argThat(input ->
                input.getId().equals(courseAssessmentId) &&
                input.getTitle().equals("수정된 중간고사") &&
                input.getScore().equals(88.0f) &&
                input.getMaxScore().equals(100.0f)
        ));
    }

    @Test
    @DisplayName("과제 평가 삭제 테스트")
    @WithMockUser
    void deleteCourseAssessmentTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.findCourseAssessmentById(courseAssessmentId))
                .thenReturn(Optional.of(testCourseAssessmentOutput));

        // When & Then
        mockMvc.perform(delete("/v1/courses/{courseId}/assessments/{id}", courseId, courseAssessmentId))
                .andExpect(status().isNoContent());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).findCourseAssessmentById(courseAssessmentId);
        verify(courseAssessmentService).deleteCourseAssessment(courseAssessmentId);
    }

    @Test
    @DisplayName("과제 평가 삭제 실패 테스트 - 존재하지 않는 평가")
    @WithMockUser
    void deleteCourseAssessmentNotFoundTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(courseAssessmentService.findCourseAssessmentById(courseAssessmentId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/v1/courses/{courseId}/assessments/{id}", courseId, courseAssessmentId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        verify(courseAssessmentService).findCourseAssessmentById(courseAssessmentId);
        verify(courseAssessmentService, never()).deleteCourseAssessment(courseAssessmentId);
    }
}