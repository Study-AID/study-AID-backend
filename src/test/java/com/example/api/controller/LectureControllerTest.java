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
import java.util.*;

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
                input.getTitle().equals("Updated Lecture Title")
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

    // ===== Summary 변환 테스트 메서드들 (LectureControllerTest.java 하단에 추가) =====

    @Test
    @DisplayName("Summary 변환 테스트 - 완전한 데이터")
    @WithMockUser
    void convertMapToSummaryCompleteDataTest() throws Exception {
        // Given - Python에서 생성될 수 있는 완전한 Summary 데이터 구조
        Map<String, Object> summaryMap = createCompleteSummaryMap();

        LectureOutput lectureWithSummary = new LectureOutput();
        lectureWithSummary.setId(lectureId);
        lectureWithSummary.setUserId(userId);
        lectureWithSummary.setCourseId(courseId);
        lectureWithSummary.setTitle("운영체제 개론");
        lectureWithSummary.setMaterialPath("os-lecture.pdf");
        lectureWithSummary.setMaterialType("pdf");
        lectureWithSummary.setSummary(summaryMap);
        lectureWithSummary.setParsedText(testParsedText);
        lectureWithSummary.setCreatedAt(LocalDateTime.now());
        lectureWithSummary.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithSummary));

        // When & Then - 변환된 Summary가 올바르게 반환되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lectureId.toString()))
                .andExpect(jsonPath("$.title").value("운영체제 개론"))
                .andExpect(jsonPath("$.summary.metadata.model").value("gpt-4"))
                .andExpect(jsonPath("$.summary.metadata.createdAt").value("2024-05-22T10:30:00Z"))
                .andExpect(jsonPath("$.summary.overview").value("운영체제의 기본 개념과 프로세스 관리에 대해 다룹니다."))
                .andExpect(jsonPath("$.summary.keywords", hasSize(2)))
                .andExpect(jsonPath("$.summary.keywords[0].keyword").value("프로세스"))
                .andExpect(jsonPath("$.summary.keywords[0].description").value("실행 중인 프로그램의 인스턴스"))
                .andExpect(jsonPath("$.summary.keywords[0].relevance").value(0.95))
                .andExpect(jsonPath("$.summary.keywords[0].pageRange.startPage").value(1))
                .andExpect(jsonPath("$.summary.keywords[0].pageRange.endPage").value(3))
                .andExpect(jsonPath("$.summary.keywords[1].keyword").value("스케줄링"))
                .andExpect(jsonPath("$.summary.topics", hasSize(2)))
                .andExpect(jsonPath("$.summary.topics[0].title").value("프로세스 관리"))
                .andExpect(jsonPath("$.summary.topics[0].description").value("프로세스의 생성, 종료, 상태 변화"))
                .andExpect(jsonPath("$.summary.topics[0].additionalDetails", hasSize(2)))
                .andExpect(jsonPath("$.summary.topics[0].subTopics", hasSize(1)))
                .andExpect(jsonPath("$.summary.topics[0].subTopics[0].title").value("프로세스 상태"))
                .andExpect(jsonPath("$.summary.additionalReferences", hasSize(2)))
                .andExpect(jsonPath("$.summary.additionalReferences[0]").value("Silberschatz, Operating System Concepts"))
                .andExpect(jsonPath("$.summary.additionalReferences[1]").value("Tanenbaum, Modern Operating Systems"));

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("Summary 변환 테스트 - 부분 데이터")
    @WithMockUser
    void convertMapToSummaryPartialDataTest() throws Exception {
        // Given - 일부 필드만 있는 Summary 데이터
        Map<String, Object> partialSummaryMap = createPartialSummaryMap();

        LectureOutput lectureWithPartialSummary = new LectureOutput();
        lectureWithPartialSummary.setId(lectureId);
        lectureWithPartialSummary.setUserId(userId);
        lectureWithPartialSummary.setCourseId(courseId);
        lectureWithPartialSummary.setTitle("데이터베이스 기초");
        lectureWithPartialSummary.setMaterialPath("db-lecture.pdf");
        lectureWithPartialSummary.setMaterialType("pdf");
        lectureWithPartialSummary.setSummary(partialSummaryMap);
        lectureWithPartialSummary.setParsedText(testParsedText);
        lectureWithPartialSummary.setCreatedAt(LocalDateTime.now());
        lectureWithPartialSummary.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithPartialSummary));

        // When & Then - 부분 데이터도 올바르게 변환되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.overview").value("데이터베이스의 기본 개념"))
                .andExpect(jsonPath("$.summary.keywords", hasSize(1)))
                .andExpect(jsonPath("$.summary.topics", hasSize(0))) // 빈 배열
                .andExpect(jsonPath("$.summary.additionalReferences", hasSize(0))); // 빈 배열

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("Summary 변환 테스트 - null Summary")
    @WithMockUser
    void convertMapToSummaryNullTest() throws Exception {
        // Given - Summary가 null인 경우
        LectureOutput lectureWithNullSummary = new LectureOutput();
        lectureWithNullSummary.setId(lectureId);
        lectureWithNullSummary.setUserId(userId);
        lectureWithNullSummary.setCourseId(courseId);
        lectureWithNullSummary.setTitle("아직 요약이 없는 강의");
        lectureWithNullSummary.setMaterialPath("no-summary.pdf");
        lectureWithNullSummary.setMaterialType("pdf");
        lectureWithNullSummary.setSummary(null); // null Summary
        lectureWithNullSummary.setParsedText(testParsedText);
        lectureWithNullSummary.setCreatedAt(LocalDateTime.now());
        lectureWithNullSummary.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithNullSummary));

        // When & Then - null Summary가 올바르게 처리되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").isEmpty()); // null이므로 빈 값

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("Summary 변환 테스트 - 잘못된 데이터 타입")
    @WithMockUser
    void convertMapToSummaryInvalidDataTest() throws Exception {
        // Given - 잘못된 타입의 데이터가 포함된 Summary
        Map<String, Object> invalidSummaryMap = createInvalidSummaryMap();

        LectureOutput lectureWithInvalidSummary = new LectureOutput();
        lectureWithInvalidSummary.setId(lectureId);
        lectureWithInvalidSummary.setUserId(userId);
        lectureWithInvalidSummary.setCourseId(courseId);
        lectureWithInvalidSummary.setTitle("잘못된 데이터 타입 테스트");
        lectureWithInvalidSummary.setMaterialPath("invalid-data.pdf");
        lectureWithInvalidSummary.setMaterialType("pdf");
        lectureWithInvalidSummary.setSummary(invalidSummaryMap);
        lectureWithInvalidSummary.setParsedText(testParsedText);
        lectureWithInvalidSummary.setCreatedAt(LocalDateTime.now());
        lectureWithInvalidSummary.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithInvalidSummary));

        // When & Then - 잘못된 데이터가 있어도 오류 없이 처리되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").isEmpty()); // 변환 실패 시 null 반환

        verify(lectureService).findLectureById(lectureId);
    }

    // ===== 테스트 헬퍼 메서드들 =====

    /**
     * 완전한 Summary 데이터 맵 생성
     */
    private Map<String, Object> createCompleteSummaryMap() {
        Map<String, Object> summaryMap = new HashMap<>();

        // Metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", "gpt-4");
        metadata.put("created_at", "2024-05-22T10:30:00Z");
        summaryMap.put("metadata", metadata);

        // Overview
        summaryMap.put("overview", "운영체제의 기본 개념과 프로세스 관리에 대해 다룹니다.");

        // Keywords
        List<Map<String, Object>> keywords = new ArrayList<>();

        Map<String, Object> keyword1 = new HashMap<>();
        keyword1.put("keyword", "프로세스");
        keyword1.put("description", "실행 중인 프로그램의 인스턴스");
        keyword1.put("relevance", 0.95);
        Map<String, Object> pageRange1 = new HashMap<>();
        pageRange1.put("start_page", 1);
        pageRange1.put("end_page", 3);
        keyword1.put("page_range", pageRange1);
        keywords.add(keyword1);

        Map<String, Object> keyword2 = new HashMap<>();
        keyword2.put("keyword", "스케줄링");
        keyword2.put("description", "CPU 시간 할당 알고리즘");
        keyword2.put("relevance", 0.88);
        Map<String, Object> pageRange2 = new HashMap<>();
        pageRange2.put("start_page", 4);
        pageRange2.put("end_page", 6);
        keyword2.put("page_range", pageRange2);
        keywords.add(keyword2);

        summaryMap.put("keywords", keywords);

        // Topics
        List<Map<String, Object>> topics = new ArrayList<>();

        Map<String, Object> topic1 = new HashMap<>();
        topic1.put("title", "프로세스 관리");
        topic1.put("description", "프로세스의 생성, 종료, 상태 변화");
        Map<String, Object> topicPageRange1 = new HashMap<>();
        topicPageRange1.put("start_page", 1);
        topicPageRange1.put("end_page", 5);
        topic1.put("page_range", topicPageRange1);
        topic1.put("additional_details", List.of("프로세스 제어 블록", "컨텍스트 스위칭"));

        // Sub-topics
        List<Map<String, Object>> subTopics = new ArrayList<>();
        Map<String, Object> subTopic1 = new HashMap<>();
        subTopic1.put("title", "프로세스 상태");
        subTopic1.put("description", "Ready, Running, Waiting 상태");
        Map<String, Object> subTopicPageRange = new HashMap<>();
        subTopicPageRange.put("start_page", 2);
        subTopicPageRange.put("end_page", 3);
        subTopic1.put("page_range", subTopicPageRange);
        subTopic1.put("additional_details", List.of());
        subTopic1.put("sub_topics", List.of());
        subTopics.add(subTopic1);

        topic1.put("sub_topics", subTopics);
        topics.add(topic1);

        Map<String, Object> topic2 = new HashMap<>();
        topic2.put("title", "메모리 관리");
        topic2.put("description", "가상 메모리와 페이징");
        Map<String, Object> topicPageRange2 = new HashMap<>();
        topicPageRange2.put("start_page", 6);
        topicPageRange2.put("end_page", 10);
        topic2.put("page_range", topicPageRange2);
        topic2.put("additional_details", List.of());
        topic2.put("sub_topics", List.of());
        topics.add(topic2);

        summaryMap.put("topics", topics);

        // Additional References
        summaryMap.put("additional_references", List.of(
                "Silberschatz, Operating System Concepts",
                "Tanenbaum, Modern Operating Systems"
        ));

        return summaryMap;
    }

    /**
     * 부분 Summary 데이터 맵 생성
     */
    private Map<String, Object> createPartialSummaryMap() {
        Map<String, Object> summaryMap = new HashMap<>();

        summaryMap.put("overview", "데이터베이스의 기본 개념");

        // Keywords만 하나 있음
        List<Map<String, Object>> keywords = new ArrayList<>();
        Map<String, Object> keyword = new HashMap<>();
        keyword.put("keyword", "SQL");
        keyword.put("description", "구조화된 질의 언어");
        keyword.put("relevance", 0.9);
        Map<String, Object> pageRange = new HashMap<>();
        pageRange.put("start_page", 1);
        pageRange.put("end_page", 2);
        keyword.put("page_range", pageRange);
        keywords.add(keyword);
        summaryMap.put("keywords", keywords);

        // topics와 additional_references는 빈 배열
        summaryMap.put("topics", List.of());
        summaryMap.put("additional_references", List.of());

        return summaryMap;
    }

    /**
     * 잘못된 타입의 데이터가 포함된 Summary 맵 생성
     */
    private Map<String, Object> createInvalidSummaryMap() {
        Map<String, Object> summaryMap = new HashMap<>();

        // 잘못된 타입들
        summaryMap.put("metadata", "잘못된 문자열"); // Map이어야 하는데 String
        summaryMap.put("overview", 12345); // String이어야 하는데 Integer
        summaryMap.put("keywords", "잘못된 키워드"); // List여야 하는데 String
        summaryMap.put("topics", null); // null

        return summaryMap;
    }

    @Test
    @DisplayName("강의 미리보기 조회 테스트 - 키워드 포함")
    @WithMockUser
    void getLecturePreviewByIdTest() throws Exception {
        // Given - 키워드가 포함된 Summary 데이터
        Map<String, Object> summaryMap = createCompleteSummaryMap();

        LectureOutput lectureWithSummary = new LectureOutput();
        lectureWithSummary.setId(lectureId);
        lectureWithSummary.setUserId(userId);
        lectureWithSummary.setCourseId(courseId);
        lectureWithSummary.setTitle("운영체제 개론");
        lectureWithSummary.setMaterialPath("os-lecture.pdf");
        lectureWithSummary.setMaterialType("pdf");
        lectureWithSummary.setSummary(summaryMap);
        lectureWithSummary.setParsedText(testParsedText);

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithSummary));

        // When & Then - Preview API가 키워드를 포함해서 반환하는지 확인
        mockMvc.perform(get("/v1/lectures/{id}/preview", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lectureId.toString()))
                .andExpect(jsonPath("$.title").value("운영체제 개론"))
                .andExpect(jsonPath("$.keywords", hasSize(2)))
                .andExpect(jsonPath("$.keywords[0].keyword").value("프로세스"))
                .andExpect(jsonPath("$.keywords[0].description").value("실행 중인 프로그램의 인스턴스"))
                .andExpect(jsonPath("$.keywords[0].relevance").value(0.95))
                .andExpect(jsonPath("$.keywords[0].pageRange.startPage").value(1))
                .andExpect(jsonPath("$.keywords[0].pageRange.endPage").value(3))
                .andExpect(jsonPath("$.keywords[1].keyword").value("스케줄링"))
                .andExpect(jsonPath("$.keywords[1].description").value("CPU 시간 할당 알고리즘"))
                .andExpect(jsonPath("$.keywords[1].relevance").value(0.88));

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("강의 미리보기 조회 테스트 - 키워드 없음")
    @WithMockUser
    void getLecturePreviewByIdNoKeywordsTest() throws Exception {
        // Given - 키워드가 없는 Summary 데이터
        Map<String, Object> summaryMap = new HashMap<>();
        summaryMap.put("overview", "키워드가 없는 강의");
        summaryMap.put("keywords", List.of()); // 빈 키워드 리스트

        LectureOutput lectureWithoutKeywords = new LectureOutput();
        lectureWithoutKeywords.setId(lectureId);
        lectureWithoutKeywords.setUserId(userId);
        lectureWithoutKeywords.setCourseId(courseId);
        lectureWithoutKeywords.setTitle("키워드 없는 강의");
        lectureWithoutKeywords.setSummary(summaryMap);
        lectureWithoutKeywords.setCreatedAt(LocalDateTime.now());
        lectureWithoutKeywords.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithoutKeywords));

        // When & Then - 키워드가 빈 배열로 반환되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}/preview", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lectureId.toString()))
                .andExpect(jsonPath("$.title").value("키워드 없는 강의"))
                .andExpect(jsonPath("$.keywords", hasSize(0))); // 빈 배열

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("강의 미리보기 조회 테스트 - Summary 없음")
    @WithMockUser
    void getLecturePreviewByIdNoSummaryTest() throws Exception {
        // Given - Summary가 null인 강의
        LectureOutput lectureWithoutSummary = new LectureOutput();
        lectureWithoutSummary.setId(lectureId);
        lectureWithoutSummary.setUserId(userId);
        lectureWithoutSummary.setCourseId(courseId);
        lectureWithoutSummary.setTitle("요약이 없는 강의");
        lectureWithoutSummary.setSummary(null); // null summary
        lectureWithoutSummary.setCreatedAt(LocalDateTime.now());
        lectureWithoutSummary.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithoutSummary));

        // When & Then - Summary가 없어도 정상 처리되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}/preview", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lectureId.toString()))
                .andExpect(jsonPath("$.title").value("요약이 없는 강의"))
                .andExpect(jsonPath("$.keywords", hasSize(0))); // 빈 배열

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("강의 미리보기 조회 실패 테스트 - 존재하지 않는 강의")
    @WithMockUser
    void getLecturePreviewByIdNotFoundTest() throws Exception {
        // Given
        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/v1/lectures/{id}/preview", lectureId))
                .andExpect(status().isNotFound());

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("강의 미리보기 조회 실패 테스트 - 다른 사용자의 강의")
    @WithMockUser
    void getLecturePreviewByIdForbiddenTest() throws Exception {
        // Given
        UUID otherUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        LectureOutput forbiddenLectureOutput = new LectureOutput();
        forbiddenLectureOutput.setId(lectureId);
        forbiddenLectureOutput.setUserId(otherUserId); // 다른 사용자의 강의
        forbiddenLectureOutput.setCourseId(courseId);
        forbiddenLectureOutput.setTitle("권한이 없는 강의");

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(forbiddenLectureOutput));

        // When & Then
        mockMvc.perform(get("/v1/lectures/{id}/preview", lectureId))
                .andExpect(status().isForbidden());

        verify(lectureService).findLectureById(lectureId);
    }

    @Test
    @DisplayName("강의 미리보기 조회 테스트 - 잘못된 키워드 데이터 처리")
    @WithMockUser
    void getLecturePreviewByIdInvalidKeywordDataTest() throws Exception {
        // Given - 잘못된 키워드 데이터
        Map<String, Object> summaryMap = new HashMap<>();
        summaryMap.put("keywords", "잘못된 키워드 데이터"); // List가 아닌 String

        LectureOutput lectureWithInvalidKeywords = new LectureOutput();
        lectureWithInvalidKeywords.setId(lectureId);
        lectureWithInvalidKeywords.setUserId(userId);
        lectureWithInvalidKeywords.setCourseId(courseId);
        lectureWithInvalidKeywords.setTitle("잘못된 키워드 데이터 테스트");
        lectureWithInvalidKeywords.setSummary(summaryMap);
        lectureWithInvalidKeywords.setCreatedAt(LocalDateTime.now());
        lectureWithInvalidKeywords.setUpdatedAt(LocalDateTime.now());

        when(lectureService.findLectureById(lectureId))
                .thenReturn(Optional.of(lectureWithInvalidKeywords));

        // When & Then - 잘못된 데이터가 있어도 오류 없이 처리되는지 확인
        mockMvc.perform(get("/v1/lectures/{id}/preview", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lectureId.toString()))
                .andExpect(jsonPath("$.title").value("잘못된 키워드 데이터 테스트"))
                .andExpect(jsonPath("$.keywords", hasSize(0))); // 변환 실패 시 빈 배열

        verify(lectureService).findLectureById(lectureId);
    }
}
