package com.example.api.controller;

import com.example.api.adapters.sqs.SQSClient;
import com.example.api.config.StorageConfig;
import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.lecture.UpdateLectureRequest;
import com.example.api.entity.ParsedPage;
import com.example.api.entity.ParsedText;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.CourseService;
import com.example.api.service.LectureService;
import com.example.api.service.SemesterService;
import com.example.api.service.StorageService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.lecture.CreateLectureInput;
import com.example.api.service.dto.lecture.LectureListOutput;
import com.example.api.service.dto.lecture.LectureOutput;
import com.example.api.service.dto.lecture.UpdateLectureInput;
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
import org.springframework.mock.web.MockMultipartFile;
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

@WebMvcTest(LectureController.class)
@Import({TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LectureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LectureService lectureService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private SemesterService semesterService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private StorageService storageService;

    @MockBean
    private SQSClient sqsClient;

    @MockBean
    private StorageConfig storageConfig;

    private UUID userId;
    private UUID semesterId;
    private UUID courseId;
    private UUID lectureId;
    private ParsedText testParsedText;

    private SemesterOutput testSemesterOutput;
    private CourseOutput testCourseOutput;
    private LectureOutput testLectureOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lectureId = UUID.randomUUID();

        // create a test SemesterOutput
        testSemesterOutput = new SemesterOutput();
        testSemesterOutput.setId(semesterId);
        testSemesterOutput.setUserId(userId);
        testSemesterOutput.setName("2025 봄학기");

        // create a test CourseOutput
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

        // create a test LectureOutput
        testLectureOutput = new LectureOutput();
        testLectureOutput.setId(lectureId);
        testLectureOutput.setUserId(userId);
        testLectureOutput.setCourseId(courseId);
        testLectureOutput.setTitle("Introduction to Operating Systems");

        // Create test parsed text
        testParsedText = new ParsedText();
        testParsedText.setTotalPages(2);
        testParsedText.setPages(List.of(
                new ParsedPage(1, "Page 1 content"),
                new ParsedPage(2, "Page 2 content")
        ));
        testLectureOutput.setParsedText(testParsedText);
        testLectureOutput.setMaterialPath("test-key.pdf");
        testLectureOutput.setMaterialType("pdf");

        // Configure StorageConfig mock
        when(storageConfig.getFullMaterialUrl(anyString()))
                .thenAnswer(invocation -> {
                    String materialPath = invocation.getArgument(0);
                    return "https://example-cloudfront.net/" + materialPath;
                });
        when(storageConfig.getFullMaterialUrl("test-key.pdf"))
                .thenReturn("https://example-cloudfront.net/test-key.pdf");
        when(storageConfig.getFullMaterialUrl("updated_material_path"))
                .thenReturn("https://example-cloudfront.net/updated_material_path");
    }

    // Add test methods here
    @Test
    @DisplayName("과목 내 강의 목록 조회 테스트")
    @WithMockUser
    void getLecturesByCourseIdTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(lectureService.findLecturesByCourseId(courseId))
                .thenReturn(new LectureListOutput(List.of(testLectureOutput)));

        // When & Then
        mockMvc.perform(get("/v1/lectures/course/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lectures", hasSize(1)))
                .andExpect(jsonPath("$.lectures[0].id").value(testLectureOutput.getId().toString()))
                .andExpect(jsonPath("$.lectures[0].title").value("Introduction to Operating Systems"))
                .andExpect(jsonPath("$.lectures[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.lectures[0].courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.lectures[0].materialUrl").value("https://example-cloudfront.net/test-key.pdf"));

        verify(courseService).findCourseById(courseId);
        verify(lectureService).findLecturesByCourseId(courseId);
    }

    @Test
    @DisplayName("과목 내 강의 목록 조회 실패 테스트 - 존재하지 않는 과목")
    @WithMockUser
    void getLecturesByCourseIdNotFoundTest() throws Exception {
        // Given
        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/lectures/course/{courseId}", courseId))
                .andExpect(status().isNotFound());

        verify(courseService).findCourseById(courseId);
        // lectureService.findLecturesByCourseId(courseId) should not be called
        verify(lectureService, never()).findLecturesByCourseId(courseId);
    }

    @Test
    @DisplayName("과목 내 강의 목록 조회 실패 테스트 - 다른 사용자의 과목")
    @WithMockUser
    void getLecturesByCourseIdForbiddenTest() throws Exception {
        // Given
        UUID otherUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID otherSemesterId = UUID.randomUUID();
        CourseOutput otherCourseOutput = new CourseOutput();
        otherCourseOutput.setId(courseId);
        otherCourseOutput.setUserId(otherUserId);
        otherCourseOutput.setSemesterId(otherSemesterId);
        otherCourseOutput.setName("운영체제");
        otherCourseOutput.setTargetGrade(4.0f);
        otherCourseOutput.setEarnedGrade(0.0f);
        otherCourseOutput.setCompletedCredits(3);
        otherCourseOutput.setCreatedAt(LocalDateTime.now());
        otherCourseOutput.setUpdatedAt(LocalDateTime.now());

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(otherCourseOutput));

        // When & Then
        mockMvc.perform(get("/v1/lectures/course/{courseId}", courseId))
                .andExpect(status().isForbidden());

        verify(courseService).findCourseById(courseId);
        // lectureService.findLecturesByCourseId(courseId) should not be called
        verify(lectureService, never()).findLecturesByCourseId(courseId);
    }

    @Test
    @DisplayName("ID로 강의 조회 테스트")
    @WithMockUser
    void getLectureByIdTest() throws Exception {
        // Given
        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(testLectureOutput));

        // When & Then
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLectureOutput.getId().toString()))
                .andExpect(jsonPath("$.title").value("Introduction to Operating Systems"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.parsedText.total_pages").value(2))
                .andExpect(jsonPath("$.parsedText.pages", hasSize(2)))
                .andExpect(jsonPath("$.parsedText.pages[0].page_number").value(1))
                .andExpect(jsonPath("$.parsedText.pages[0].text").value("Page 1 content"))
                .andExpect(jsonPath("$.parsedText.pages[1].page_number").value(2))
                .andExpect(jsonPath("$.parsedText.pages[1].text").value("Page 2 content"))
                .andExpect(jsonPath("$.materialUrl").value("https://example-cloudfront.net/test-key.pdf"));

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("ID로 강의 조회 실패 테스트 - 존재하지 않는 강의")
    @WithMockUser
    void getLectureByIdNotFoundTest() throws Exception {
        // Given
        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isNotFound());

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("ID로 강의 조회 실패 테스트 - 다른 사용자의 강의(권한이 없는 강의)")
    @WithMockUser
    void getLectureByIdForbiddenTest() throws Exception {
        // Given
        UUID otherUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        LectureOutput forbiddenLectureOutput = new LectureOutput();
        forbiddenLectureOutput.setId(lectureId);
        forbiddenLectureOutput.setUserId(otherUserId);
        forbiddenLectureOutput.setCourseId(courseId);
        forbiddenLectureOutput.setTitle("권한이 없는 강의");

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(forbiddenLectureOutput));

        // When & Then
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isForbidden());

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("새 강의 생성 테스트")
    @WithMockUser
    void createLectureTest() throws Exception {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test-lecture.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));
        when(storageService.upload(any()))
                .thenReturn("test-key.pdf");
        when(lectureService.createLecture(any(CreateLectureInput.class)))
                .thenReturn(testLectureOutput);

        // When & Then
        mockMvc.perform(multipart("/v1/lectures")
                        .file(pdfFile)
                        .param("courseId", courseId.toString())
                        .param("title", "Test Lecture"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testLectureOutput.getId().toString()))
                .andExpect(jsonPath("$.title").value("Introduction to Operating Systems"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.materialUrl").value("https://example-cloudfront.net/test-key.pdf"));

        verify(courseService).findCourseById(courseId);
        verify(storageService).upload(any());
        verify(sqsClient).sendGenerateSummaryMessage(any());
        verify(lectureService).createLecture(argThat(input ->
                input.getCourseId().equals(courseId) &&
                        input.getUserId().equals(userId) &&
                        input.getTitle().equals("Test Lecture") &&
                        input.getMaterialPath().equals("test-key.pdf") &&
                        input.getMaterialType().equals("pdf")
        ));
    }

    @Test
    @DisplayName("강의 생성 실패 테스트 - PDF가 아닌 파일")
    @WithMockUser
    void createLectureNonPdfFileTest() throws Exception {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test-lecture.txt",
                "text/plain",
                "Text content".getBytes()
        );

        when(courseService.findCourseById(courseId))
                .thenReturn(Optional.of(testCourseOutput));

        // When & Then
        mockMvc.perform(multipart("/v1/lectures")
                        .file(textFile)
                        .param("courseId", courseId.toString())
                        .param("title", "Test Lecture"))
                .andExpect(status().isBadRequest());

        verify(courseService).findCourseById(courseId);
        verify(storageService, never()).upload(any());
        verify(sqsClient, never()).sendGenerateSummaryMessage(any());
        verify(lectureService, never()).createLecture(any());
    }

    @Test
    @DisplayName("강의 정보 업데이트")
    @WithMockUser
    void updateLectureTest() throws Exception {
        // Given
        UpdateLectureRequest updateLectureRequest = new UpdateLectureRequest();
        updateLectureRequest.setTitle("Updated Lecture Title");
        updateLectureRequest.setMaterialPath("updated_material_path");
        updateLectureRequest.setMaterialType("updated_material_type");

        LectureOutput updatedLectureOutput = new LectureOutput();
        updatedLectureOutput.setId(lectureId);
        updatedLectureOutput.setUserId(userId);
        updatedLectureOutput.setCourseId(courseId);
        updatedLectureOutput.setTitle("Updated Lecture Title");
        updatedLectureOutput.setMaterialPath("updated_material_path");
        updatedLectureOutput.setMaterialType("updated_material_type");
        updatedLectureOutput.setParsedText(testParsedText);

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(testLectureOutput));
        when(lectureService.updateLecture(any(UpdateLectureInput.class)))
                .thenReturn(updatedLectureOutput);

        // When & Then
        mockMvc.perform(put("/v1/lectures/{id}", lectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateLectureRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLectureOutput.getId().toString()))
                .andExpect(jsonPath("$.title").value("Updated Lecture Title"))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.courseId").value(courseId.toString()))
                .andExpect(jsonPath("$.materialUrl").value("https://example-cloudfront.net/updated_material_path"));

        verify(lectureService).findLectureById(lectureId);
        verify(lectureService).updateLecture(argThat(input ->
                input.getTitle().equals("Updated Lecture Title") &&
                        input.getMaterialPath().equals("updated_material_path") &&
                        input.getMaterialType().equals("updated_material_type")
        ));
    }

    @Test
    @DisplayName("강의 삭제")
    @WithMockUser
    void deleteLectureTest() throws Exception {
        // Given
        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(testLectureOutput));

        // When & Then
        mockMvc.perform(delete("/v1/lectures/{id}", lectureId))
                .andExpect(status().isNoContent());

        verify(lectureService).findLectureById(lectureId);
        verify(lectureService).deleteLecture(lectureId);
    }
}
