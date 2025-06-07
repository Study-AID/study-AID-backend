package com.example.api.controller;

import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.report.CreateReportRequest;
import com.example.api.controller.dto.report.ReportResponse;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.QuizQuestionReportService;
import com.example.api.service.ExamQuestionReportService;
import com.example.api.service.dto.report.QuizQuestionReportOutput;
import com.example.api.service.dto.report.ExamQuestionReportOutput;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import({TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private QuizQuestionReportService quizQuestionReportService;

    @MockBean
    private ExamQuestionReportService examQuestionReportService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID userId;
    private UUID quizId;
    private UUID quizItemId;
    private UUID examId;
    private UUID examItemId;
    private UUID reportId;

    private QuizQuestionReportOutput testQuizReportOutput;
    private ExamQuestionReportOutput testExamReportOutput;

    @BeforeEach
    public void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        quizId = UUID.randomUUID();
        quizItemId = UUID.randomUUID();
        examId = UUID.randomUUID();
        examItemId = UUID.randomUUID();
        reportId = UUID.randomUUID();

        testQuizReportOutput = new QuizQuestionReportOutput();
        testQuizReportOutput.setId(reportId);
        testQuizReportOutput.setUserId(userId);
        testQuizReportOutput.setQuizId(quizId);
        testQuizReportOutput.setQuizItemId(quizItemId);
        testQuizReportOutput.setReportReason("부적절한 문제");
        testQuizReportOutput.setCreatedAt(LocalDateTime.now());
        testQuizReportOutput.setUpdatedAt(LocalDateTime.now());

        testExamReportOutput = new ExamQuestionReportOutput();
        testExamReportOutput.setId(reportId);
        testExamReportOutput.setUserId(userId);
        testExamReportOutput.setExamId(examId);
        testExamReportOutput.setExamItemId(examItemId);
        testExamReportOutput.setReportReason("문제가 애매함");
        testExamReportOutput.setCreatedAt(LocalDateTime.now());
        testExamReportOutput.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("퀴즈 문제 신고 생성 - 정상 케이스")
    @WithMockUser
    void createQuizReportSuccess() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("QUIZ");
        request.setQuizId(quizId);
        request.setQuizItemId(quizItemId);
        request.setReportReason("부적절한 문제");

        when(quizQuestionReportService.createReport(any())).thenReturn(testQuizReportOutput);

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(reportId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.itemType", is("QUIZ")))
                .andExpect(jsonPath("$.quizId", is(quizId.toString())))
                .andExpect(jsonPath("$.quizItemId", is(quizItemId.toString())))
                .andExpect(jsonPath("$.reportReason", is("부적절한 문제")));

        verify(quizQuestionReportService, times(1)).createReport(any());
        verify(examQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("시험 문제 신고 생성 - 정상 케이스")
    @WithMockUser
    void createExamReportSuccess() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("EXAM");
        request.setExamId(examId);
        request.setExamItemId(examItemId);
        request.setReportReason("문제가 애매함");

        when(examQuestionReportService.createReport(any())).thenReturn(testExamReportOutput);

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(reportId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.itemType", is("EXAM")))
                .andExpect(jsonPath("$.examId", is(examId.toString())))
                .andExpect(jsonPath("$.examItemId", is(examItemId.toString())))
                .andExpect(jsonPath("$.reportReason", is("문제가 애매함")));

        verify(examQuestionReportService, times(1)).createReport(any());
        verify(quizQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("신고 생성 - 잘못된 itemType")
    @WithMockUser
    void createReportInvalidItemType() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("INVALID");
        request.setQuizId(quizId);
        request.setQuizItemId(quizItemId);
        request.setReportReason("부적절한 문제");

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(quizQuestionReportService, never()).createReport(any());
        verify(examQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("퀴즈 신고 생성 - 필수 필드 누락 (quizId)")
    @WithMockUser
    void createQuizReportMissingQuizId() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("QUIZ");
        // request.setQuizId(quizId); // 누락
        request.setQuizItemId(quizItemId);
        request.setReportReason("부적절한 문제");

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(quizQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("시험 신고 생성 - 필수 필드 누락 (examItemId)")
    @WithMockUser
    void createExamReportMissingExamItemId() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("EXAM");
        request.setExamId(examId);
        // request.setExamItemId(examItemId); // 누락
        request.setReportReason("문제가 애매함");

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(examQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("신고 생성 - 빈 신고 사유")
    @WithMockUser
    void createReportEmptyReason() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("QUIZ");
        request.setQuizId(quizId);
        request.setQuizItemId(quizItemId);
        request.setReportReason(""); // 빈 문자열

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(quizQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("신고 생성 - null itemType")
    @WithMockUser
    void createReportNullItemType() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType(null);
        request.setQuizId(quizId);
        request.setQuizItemId(quizItemId);
        request.setReportReason("부적절한 문제");

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(quizQuestionReportService, never()).createReport(any());
        verify(examQuestionReportService, never()).createReport(any());
    }

    @Test
    @DisplayName("내 신고 목록 조회 - 정상 케이스")
    @WithMockUser
    void getMyReportsSuccess() throws Exception {
        // given
        List<QuizQuestionReportOutput> quizReports = Arrays.asList(testQuizReportOutput);
        List<ExamQuestionReportOutput> examReports = Arrays.asList(testExamReportOutput);

        when(quizQuestionReportService.findReportsByUser(userId)).thenReturn(quizReports);
        when(examQuestionReportService.findReportsByUser(userId)).thenReturn(examReports);

        // when, then
        mockMvc.perform(get("/v1/reports/my-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports", hasSize(2)))
                .andExpect(jsonPath("$.reports[0].itemType", is("QUIZ")))
                .andExpect(jsonPath("$.reports[0].reportReason", is("부적절한 문제")))
                .andExpect(jsonPath("$.reports[1].itemType", is("EXAM")))
                .andExpect(jsonPath("$.reports[1].reportReason", is("문제가 애매함")));

        verify(quizQuestionReportService, times(1)).findReportsByUser(userId);
        verify(examQuestionReportService, times(1)).findReportsByUser(userId);
    }

    @Test
    @DisplayName("내 신고 목록 조회 - 빈 목록")
    @WithMockUser
    void getMyReportsEmpty() throws Exception {
        // given
        when(quizQuestionReportService.findReportsByUser(userId)).thenReturn(Collections.emptyList());
        when(examQuestionReportService.findReportsByUser(userId)).thenReturn(Collections.emptyList());

        // when, then
        mockMvc.perform(get("/v1/reports/my-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reports", hasSize(0)));

        verify(quizQuestionReportService, times(1)).findReportsByUser(userId);
        verify(examQuestionReportService, times(1)).findReportsByUser(userId);
    }

    @Test
    @DisplayName("신고 삭제 - 퀴즈 신고 삭제 성공")
    @WithMockUser
    void deleteQuizReportSuccess() throws Exception {
        // given
        when(quizQuestionReportService.findReportById(reportId)).thenReturn(Optional.of(testQuizReportOutput));
        when(examQuestionReportService.findReportById(reportId)).thenReturn(Optional.empty());
        when(quizQuestionReportService.deleteReport(reportId)).thenReturn(true);

        // when, then
        mockMvc.perform(delete("/v1/reports/{reportId}", reportId))
                .andExpect(status().isNoContent());

        verify(quizQuestionReportService, times(1)).findReportById(reportId);
        verify(examQuestionReportService, times(1)).findReportById(reportId);
        verify(quizQuestionReportService, times(1)).deleteReport(reportId);
    }

    @Test
    @DisplayName("신고 삭제 - 시험 신고 삭제 성공")
    @WithMockUser
    void deleteExamReportSuccess() throws Exception {
        // given
        when(quizQuestionReportService.findReportById(reportId)).thenReturn(Optional.empty());
        when(examQuestionReportService.findReportById(reportId)).thenReturn(Optional.of(testExamReportOutput));
        when(examQuestionReportService.deleteReport(reportId)).thenReturn(true);

        // when, then
        mockMvc.perform(delete("/v1/reports/{reportId}", reportId))
                .andExpect(status().isNoContent());

        verify(quizQuestionReportService, times(1)).findReportById(reportId);
        verify(examQuestionReportService, times(1)).findReportById(reportId);
        verify(examQuestionReportService, times(1)).deleteReport(reportId);
    }

    @Test
    @DisplayName("신고 삭제 - 존재하지 않는 신고")
    @WithMockUser
    void deleteReportNotFound() throws Exception {
        // given
        when(quizQuestionReportService.findReportById(reportId)).thenReturn(Optional.empty());
        when(examQuestionReportService.findReportById(reportId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(delete("/v1/reports/{reportId}", reportId))
                .andExpect(status().isNotFound());

        verify(quizQuestionReportService, times(1)).findReportById(reportId);
        verify(examQuestionReportService, times(1)).findReportById(reportId);
        verify(quizQuestionReportService, never()).deleteReport(any());
        verify(examQuestionReportService, never()).deleteReport(any());
    }

    @Test
    @DisplayName("신고 삭제 - 권한 없음 (다른 사용자의 신고)")
    @WithMockUser
    void deleteReportUnauthorized() throws Exception {
        // given
        UUID anotherUserId = UUID.randomUUID();
        QuizQuestionReportOutput anotherUserReport = new QuizQuestionReportOutput();
        anotherUserReport.setId(reportId);
        anotherUserReport.setUserId(anotherUserId); // 다른 사용자의 신고
        anotherUserReport.setQuizId(quizId);
        anotherUserReport.setQuizItemId(quizItemId);
        anotherUserReport.setReportReason("다른 사용자 신고");

        when(quizQuestionReportService.findReportById(reportId)).thenReturn(Optional.of(anotherUserReport));
        when(examQuestionReportService.findReportById(reportId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(delete("/v1/reports/{reportId}", reportId))
                .andExpect(status().isForbidden());

        verify(quizQuestionReportService, times(1)).findReportById(reportId);
        verify(examQuestionReportService, times(1)).findReportById(reportId);
        verify(quizQuestionReportService, never()).deleteReport(any());
        verify(examQuestionReportService, never()).deleteReport(any());
    }

    @Test
    @DisplayName("신고 생성 - 서비스에서 예외 발생")
    @WithMockUser
    void createReportServiceException() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("QUIZ");
        request.setQuizId(quizId);
        request.setQuizItemId(quizItemId);
        request.setReportReason("부적절한 문제");

        when(quizQuestionReportService.createReport(any())).thenThrow(new IllegalArgumentException("Invalid data"));

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(quizQuestionReportService, times(1)).createReport(any());
    }

    @Test
    @DisplayName("신고 생성 - 예상치 못한 서버 에러")
    @WithMockUser
    void createReportUnexpectedError() throws Exception {
        // given
        CreateReportRequest request = new CreateReportRequest();
        request.setItemType("QUIZ");
        request.setQuizId(quizId);
        request.setQuizItemId(quizItemId);
        request.setReportReason("부적절한 문제");

        when(quizQuestionReportService.createReport(any())).thenThrow(new RuntimeException("Unexpected error"));

        // when, then
        mockMvc.perform(post("/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(quizQuestionReportService, times(1)).createReport(any());
    }
}