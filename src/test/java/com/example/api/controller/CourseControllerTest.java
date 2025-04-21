package com.example.api.controller;

import com.example.api.controller.dto.course.CreateCourseRequest;
import com.example.api.controller.dto.course.UpdateCourseGradesRequest;
import com.example.api.controller.dto.course.UpdateCourseRequest;
import com.example.api.entity.Course;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.service.CourseService;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.course.CreateCourseInput;
import com.example.api.service.dto.course.UpdateCourseGradesInput;
import com.example.api.service.dto.course.UpdateCourseInput;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
@ActiveProfiles("test")
class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    @MockBean
    private SemesterService semesterService;

    private UUID userId;
    private UUID semesterId;
    private UUID courseId;
    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private CourseOutput testCourseOutput;

    @BeforeEach
    void setUp() {
        // TODO(mj): use @WithMockUser or @WithSecurityContext instead of hard-coding userId
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testSemester = new Semester();
        testSemester.setId(semesterId);
        testSemester.setUser(testUser);
        testSemester.setName("2025 봄학기");

        testCourse = new Course();
        testCourse.setId(courseId);
        testCourse.setUser(testUser);
        testCourse.setSemester(testSemester);
        testCourse.setName("운영체제");
        testCourse.setTargetGrade(4.0f);
        testCourse.setEarnedGrade(0.0f);
        testCourse.setCompletedCredits(3);
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse.setUpdatedAt(LocalDateTime.now());

        testCourseOutput = CourseOutput.fromEntity(testCourse);
    }

    @Test
    @DisplayName("모든 과목 목록 조회")
    void getCourses() throws Exception {
        // Given
        when(courseService.findCoursesByUserId(userId))
                .thenReturn(Arrays.asList(testCourseOutput));

        // When/Then
        mockMvc.perform(get("/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(1)))
                .andExpect(jsonPath("$.courses[0].id", is(courseId.toString())))
                .andExpect(jsonPath("$.courses[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.courses[0].semesterId", is(semesterId.toString())))
                .andExpect(jsonPath("$.courses[0].name", is("운영체제")));

        verify(courseService).findCoursesByUserId(userId);
    }

    @Test
    @DisplayName("과목이 없을 때 빈 목록 반환")
    void getCourses_WhenNoCourses_ReturnsEmptyList() throws Exception {
        // Given
        when(courseService.findCoursesByUserId(userId))
                .thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses", hasSize(0)));

        verify(courseService).findCoursesByUserId(userId);
    }

    @Test
    @DisplayName("학기별 과목 목록 조회")
    void getCoursesBySemester() throws Exception {
        // Given
        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
        when(courseService.findCoursesBySemesterId(semesterId))
                .thenReturn(Arrays.asList(testCourseOutput));

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
    void getCoursesBySemester_Forbidden() throws Exception {
        // Given
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        Semester otherSemester = new Semester();
        otherSemester.setId(semesterId);
        otherSemester.setUser(otherUser);

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(otherSemester));

        // When/Then
        mockMvc.perform(get("/v1/courses/semester/{semesterId}", semesterId))
                .andExpect(status().isForbidden());

        verify(semesterService).findSemesterById(semesterId);
        verify(courseService, never()).findCoursesBySemesterId(any());
    }

    @Test
    @DisplayName("ID로 과목 조회")
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
    void getCourseById_Forbidden() throws Exception {
        // Given
        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(UUID.randomUUID()); // Different user ID
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
    @DisplayName("새 과목 생성")
    void createCourse() throws Exception {
        // Given
        CreateCourseRequest createRequest = new CreateCourseRequest();
        createRequest.setSemesterId(semesterId);
        createRequest.setName("운영체제");

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
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
    void createCourse_DuplicateCourse() throws Exception {
        // Given
        CreateCourseRequest createRequest = new CreateCourseRequest();
        createRequest.setSemesterId(semesterId);
        createRequest.setName("운영체제");

        when(semesterService.findSemesterById(semesterId))
                .thenReturn(Optional.of(testSemester));
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
    void updateCourse() throws Exception {
        // Given
        UpdateCourseRequest updateRequest = new UpdateCourseRequest();
        updateRequest.setName("고급 운영체제");

        CourseOutput updatedCourseOutput = new CourseOutput();
        updatedCourseOutput.setId(courseId);
        updatedCourseOutput.setUserId(userId);
        updatedCourseOutput.setSemesterId(semesterId);
        updatedCourseOutput.setName("고급 운영체제");
        updatedCourseOutput.setTargetGrade(testCourse.getTargetGrade());
        updatedCourseOutput.setEarnedGrade(testCourse.getEarnedGrade());
        updatedCourseOutput.setCompletedCredits(testCourse.getCompletedCredits());
        updatedCourseOutput.setCreatedAt(testCourse.getCreatedAt());
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
    void updateCourse_Forbidden() throws Exception {
        // Given
        UpdateCourseRequest updateRequest = new UpdateCourseRequest();
        updateRequest.setName("고급 운영체제");

        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(UUID.randomUUID()); // Different user ID
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
    void updateCourseGrades() throws Exception {
        // Given
        UpdateCourseGradesRequest gradesRequest = new UpdateCourseGradesRequest();
        gradesRequest.setTargetGrade(4.3f);
        gradesRequest.setEarnedGrade(3.8f);
        gradesRequest.setCompletedCredits(3);

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        doNothing().when(courseService).updateCourseGrades(any(UpdateCourseGradesInput.class));

        // When/Then
        mockMvc.perform(put("/v1/courses/{id}/grades", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesRequest)))
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
    @DisplayName("유효하지 않은 성적 정보로 업데이트")
    void updateCourseGrades_InvalidGrades() throws Exception {
        // Given
        UpdateCourseGradesRequest gradesRequest = new UpdateCourseGradesRequest();
        gradesRequest.setTargetGrade(5.0f); // 4.5 초과 (컨트롤러에서 지정된 최대값)
        gradesRequest.setEarnedGrade(3.8f);
        gradesRequest.setCompletedCredits(3);

        // When/Then
        mockMvc.perform(put("/v1/courses/{id}/grades", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradesRequest)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).updateCourseGrades(any(UpdateCourseGradesInput.class));
    }

    @Test
    @DisplayName("과목 삭제")
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
    void deleteCourse_Forbidden() throws Exception {
        // Given
        CourseOutput forbiddenCourseOutput = new CourseOutput();
        forbiddenCourseOutput.setId(courseId);
        forbiddenCourseOutput.setUserId(UUID.randomUUID()); // Different user ID
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