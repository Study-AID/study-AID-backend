package com.example.api.controller;

import com.example.api.adapters.sqs.SQSClient;
import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.exam.CreateExamRequest;
import com.example.api.controller.dto.exam.SubmitExamItem;
import com.example.api.controller.dto.exam.SubmitExamRequest;
import com.example.api.controller.dto.exam.UpdateExamRequest;
import com.example.api.entity.Exam;
import com.example.api.entity.ExamItem;
import com.example.api.entity.ExamResponse;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.CourseService;
import com.example.api.service.ExamService;
import com.example.api.service.StorageService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.exam.*;
import com.example.api.util.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(ExamController.class)
@Import({ TestSecurityConfig.class })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ExamControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExamService examService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private StorageService storageService;

    @MockBean
    private SQSClient sqsClient;

    private UUID userId;
    private UUID courseId;
    private UUID examId;
    private UUID examResultId;

    private CourseOutput testCourseOutput;
    private ExamOutput testExamOutput;
    private ExamResultOutput testExamResultOutput;
    private ExamResultListOutput testExamResultListOutput;

    private List<ExamItem> testExamItems;
    private ExamResponseListOutput testExamResponseListOutput;

    @BeforeEach
    public void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        courseId = UUID.randomUUID();
        examId = UUID.randomUUID();
        examResultId = UUID.randomUUID();

        testCourseOutput = new CourseOutput();
        testCourseOutput.setId(courseId);
        testCourseOutput.setUserId(userId);
        testCourseOutput.setName("운영체제");

        testExamOutput = new ExamOutput();
        testExamOutput.setId(examId);
        testExamOutput.setCourseId(courseId);
        testExamOutput.setUserId(userId);
        testExamOutput.setTitle("중간고사");
        testExamOutput.setStatus(Status.not_started);

        testExamItems = List.of(
                new ExamItem(),
                new ExamItem());
        testExamItems.get(0).setId(UUID.randomUUID());
        testExamOutput.setExamItems(testExamItems);

        testExamResponseListOutput = new ExamResponseListOutput();
        testExamResponseListOutput.setExamResponseOutputs(List.of(new ExamResponseOutput()));
        testExamResponseListOutput.getExamResponseOutputs().get(0).setId(UUID.randomUUID());
        testExamResponseListOutput.getExamResponseOutputs().get(0).setExamId(examId);
        testExamResponseListOutput.getExamResponseOutputs().get(0).setUserId(userId);
        testExamResponseListOutput.getExamResponseOutputs().get(0).setExamItemId(testExamItems.get(0).getId());

        testExamResultOutput = new ExamResultOutput();
        testExamResultOutput.setId(examResultId);
        testExamResultOutput.setExamId(examId);
        testExamResultOutput.setUserId(userId);
        testExamResultOutput.setScore(85.0f);
        testExamResultOutput.setMaxScore(100.0f);
        testExamResultOutput.setFeedback("Good job!");
        testExamResultOutput.setStartTime(LocalDateTime.now().minusHours(1));
        testExamResultOutput.setEndTime(LocalDateTime.now());

        testExamResultListOutput = new ExamResultListOutput();
        testExamResultListOutput.setExamResults(List.of(testExamResultOutput));
    }

    @Test
    @DisplayName("코스별 시험 목록 조회")
    @WithMockUser
    void getExamsByCourse() throws Exception {
        // given
        when(courseService.findCourseById(courseId)).thenReturn(Optional.of(testCourseOutput));
        when(examService.findExamsByCourseId(courseId)).thenReturn(new ExamListOutput(List.of(testExamOutput)));

        // when, then
        mockMvc.perform(get("/v1/exams/course/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exams", hasSize(1)))
                .andExpect(jsonPath("$.exams[0].id", is(examId.toString())))
                .andExpect(jsonPath("$.exams[0].courseId", is(courseId.toString())))
                .andExpect(jsonPath("$.exams[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.exams[0].title", is("중간고사")))
                .andExpect(jsonPath("$.exams[0].status", is("not_started")));

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, times(1)).findExamsByCourseId(courseId);
    }

    @Test
    @DisplayName("시험 ID로 시험 조회")
    @WithMockUser
    void getExamById() throws Exception {
        // given
        when(examService.findExamById(examId)).thenReturn(Optional.of(testExamOutput));

        // when, then
        mockMvc.perform(get("/v1/exams/{id}", examId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(examId.toString())))
                .andExpect(jsonPath("$.courseId", is(courseId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.title", is("중간고사")))
                .andExpect(jsonPath("$.status", is("not_started")))
                .andExpect(jsonPath("$.examItems[0].id", is(testExamItems.get(0).getId().toString())));

        verify(examService, times(1)).findExamById(examId);
    }

    @Test
    @DisplayName("시험 생성")
    @WithMockUser
    void createExam() throws Exception {
        // given
        CreateExamRequest createExamRequest = new CreateExamRequest();
        createExamRequest.setCourseId(courseId);
        createExamRequest.setTitle("중간고사");
        createExamRequest.setReferencedLectures(new UUID[] { UUID.randomUUID(), UUID.randomUUID() });
        createExamRequest.setTrueOrFalseCount(2);
        createExamRequest.setMultipleChoiceCount(3);
        createExamRequest.setShortAnswerCount(1);
        createExamRequest.setEssayCount(1);

        when(courseService.findCourseById(courseId)).thenReturn(Optional.of(testCourseOutput));
        when(examService.createExam(any(CreateExamInput.class))).thenReturn(testExamOutput);

        // when, then
        mockMvc.perform(post("/v1/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExamRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(examId.toString())))
                .andExpect(jsonPath("$.courseId", is(courseId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.title", is("중간고사")));

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, times(1)).createExam(any(CreateExamInput.class));
        verify(sqsClient, times(1)).sendGenerateExamMessage(any());
    }

    @Test
    @DisplayName("시험 수정")
    @WithMockUser
    void updateExam() throws Exception {
        // given
        UpdateExamRequest updateExamRequest = new UpdateExamRequest();
        updateExamRequest.setTitle("Updated Exam Title");

        when(examService.findExamById(examId)).thenReturn(Optional.of(testExamOutput));
        when(examService.updateExam(any(UpdateExamInput.class))).thenReturn(testExamOutput);

        // when, then
        mockMvc.perform(put("/v1/exams/{id}", examId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateExamRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(examId.toString())))
                .andExpect(jsonPath("$.courseId", is(courseId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.title", is("중간고사")));

        verify(examService, times(1)).findExamById(examId);
        verify(examService, times(1)).updateExam(argThat(input -> input.getId().equals(examId)
                && input.getTitle().equals("Updated Exam Title")));
    }

    @Test
    @DisplayName("시험 삭제")
    @WithMockUser
    void deleteExam() throws Exception {
        // given
        when(examService.findExamById(examId)).thenReturn(Optional.of(testExamOutput));

        // when, then
        mockMvc.perform(delete("/v1/exams/{id}", examId))
                .andExpect(status().isNoContent());

        verify(examService, times(1)).findExamById(examId);
        verify(examService, times(1)).deleteExam(examId);
    }

    @Test
    @DisplayName("시험 응답 생성")
    @WithMockUser
    void submitAndGradeExam() throws Exception {
        // given
        SubmitExamRequest submitExamRequest = new SubmitExamRequest();

        Exam testExam = new Exam();
        testExam.setId(examId);

        ExamResponse testExamResponse1 = new ExamResponse();
        testExamResponse1.setId(UUID.randomUUID());
        testExamResponse1.setExam(testExam);

        submitExamRequest.setSubmitExamItems(List.of(
                new SubmitExamItem(),
                new SubmitExamItem()));
        submitExamRequest.getSubmitExamItems().get(0).setExamItemId(testExamItems.get(0).getId());
        submitExamRequest.getSubmitExamItems().get(0).setQuestionType(QuestionType.true_or_false);
        submitExamRequest.getSubmitExamItems().get(0).setSelectedBool(true);
        submitExamRequest.getSubmitExamItems().get(1).setExamItemId(testExamItems.get(1).getId());
        submitExamRequest.getSubmitExamItems().get(1).setQuestionType(QuestionType.multiple_choice);
        submitExamRequest.getSubmitExamItems().get(1).setSelectedIndices(new Integer[] { 0, 1 });

        when(examService.findExamById(examId)).thenReturn(Optional.of(testExamOutput));
        when(examService.submitAndGradeExamWithStatus(Mockito.<List<CreateExamResponseInput>>any())).thenReturn(testExamResponseListOutput);

        // when, then
        mockMvc.perform(post("/v1/exams/{id}/submit", examId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitExamRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submitExamResponses[0].id",
                        is(testExamResponseListOutput.getExamResponseOutputs().get(0).getId().toString())))
                .andExpect(jsonPath("$.submitExamResponses[0].examId", is(examId.toString())))
                .andExpect(jsonPath("$.submitExamResponses[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.submitExamResponses[0].examItemId", is(testExamItems.get(0).getId().toString())));
    }

    @Test
    @DisplayName("유효하지 않은 요청으로 시험 생성")
    @WithMockUser
    void createExam_InvalidRequest() throws Exception {
        // given
        CreateExamRequest createExamRequest = new CreateExamRequest();
        createExamRequest.setCourseId(courseId);
        createExamRequest.setTitle(""); // 빈 제목
        createExamRequest.setReferencedLectures(new UUID[] { UUID.randomUUID() });
        createExamRequest.setTrueOrFalseCount(2);
        createExamRequest.setMultipleChoiceCount(3);
        createExamRequest.setShortAnswerCount(1);
        createExamRequest.setEssayCount(1);

        when(courseService.findCourseById(courseId)).thenReturn(Optional.of(testCourseOutput));

        // when, then
        mockMvc.perform(post("/v1/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExamRequest)))
                .andExpect(status().isBadRequest());

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, never()).createExam(any(CreateExamInput.class));
        verify(sqsClient, never()).sendGenerateExamMessage(any());
    }

    @Test
    @DisplayName("존재하지 않는 코스로 시험 생성")
    @WithMockUser
    void createExam_CourseNotFound() throws Exception {
        // given
        CreateExamRequest createExamRequest = new CreateExamRequest();
        createExamRequest.setCourseId(courseId);
        createExamRequest.setTitle("중간고사");
        createExamRequest.setReferencedLectures(new UUID[] { UUID.randomUUID() });
        createExamRequest.setTrueOrFalseCount(2);
        createExamRequest.setMultipleChoiceCount(3);
        createExamRequest.setShortAnswerCount(1);
        createExamRequest.setEssayCount(1);

        when(courseService.findCourseById(courseId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(post("/v1/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createExamRequest)))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, never()).createExam(any(CreateExamInput.class));
        verify(sqsClient, never()).sendGenerateExamMessage(any());
    }

    @Test
    @DisplayName("시험 ID로 시험 결과 조회")
    @WithMockUser
    void getExamResultById() throws Exception {
        // given
        ExamOutput gradedExamOutput = new ExamOutput();
        gradedExamOutput.setId(examId);
        gradedExamOutput.setCourseId(courseId);
        gradedExamOutput.setUserId(userId);
        gradedExamOutput.setTitle("중간고사");
        gradedExamOutput.setStatus(Status.graded); // 채점 완료 상태

        when(examService.findExamById(examId)).thenReturn(Optional.of(gradedExamOutput));
        when(examService.findExamResultByExamId(examId)).thenReturn(Optional.of(testExamResultOutput));

        // when, then
        mockMvc.perform(get("/v1/exams/{id}/result", examId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(examResultId.toString())))
                .andExpect(jsonPath("$.examId", is(examId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.score", is(85.0)))
                .andExpect(jsonPath("$.maxScore", is(100.0)))
                .andExpect(jsonPath("$.feedback", is("Good job!")));

        verify(examService, times(1)).findExamById(examId);
        verify(examService, times(1)).findExamResultByExamId(examId);
    }

    @Test
    @DisplayName("아직 채점되지 않은 시험의 결과 조회 시 400 에러")
    @WithMockUser
    void getExamResultById_NotGradedYet() throws Exception {
        // given
        ExamOutput notGradedExamOutput = new ExamOutput();
        notGradedExamOutput.setId(examId);
        notGradedExamOutput.setCourseId(courseId);
        notGradedExamOutput.setUserId(userId);
        notGradedExamOutput.setTitle("중간고사");
        notGradedExamOutput.setStatus(Status.not_started); // 제출됨 상태 (채점 전)

        when(examService.findExamById(examId)).thenReturn(Optional.of(notGradedExamOutput));

        // when, then
        mockMvc.perform(get("/v1/exams/{id}/result", examId))
                .andExpect(status().isBadRequest());

        verify(examService, times(1)).findExamById(examId);
        verify(examService, never()).findExamResultByExamId(examId);
    }

    @Test
    @DisplayName("존재하지 않는 시험의 결과 조회 시 404 에러")
    @WithMockUser
    void getExamResultById_ExamNotFound() throws Exception {
        // given
        when(examService.findExamById(examId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(get("/v1/exams/{id}/result", examId))
                .andExpect(status().isNotFound());

        verify(examService, times(1)).findExamById(examId);
        verify(examService, never()).findExamResultByExamId(examId);
    }

    @Test
    @DisplayName("시험 결과가 존재하지 않는 경우 404 에러")
    @WithMockUser
    void getExamResultById_ResultNotFound() throws Exception {
        // given
        ExamOutput gradedExamOutput = new ExamOutput();
        gradedExamOutput.setId(examId);
        gradedExamOutput.setCourseId(courseId);
        gradedExamOutput.setUserId(userId);
        gradedExamOutput.setTitle("중간고사");
        gradedExamOutput.setStatus(Status.graded);

        when(examService.findExamById(examId)).thenReturn(Optional.of(gradedExamOutput));
        when(examService.findExamResultByExamId(examId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(get("/v1/exams/{id}/result", examId))
                .andExpect(status().isNotFound());

        verify(examService, times(1)).findExamById(examId);
        verify(examService, times(1)).findExamResultByExamId(examId);
    }

    @Test
    @DisplayName("다른 사용자의 시험 결과 조회 시 403 에러")
    @WithMockUser
    void getExamResultById_Forbidden() throws Exception {
        // given
        UUID otherUserId = UUID.randomUUID();
        ExamOutput otherUserExamOutput = new ExamOutput();
        otherUserExamOutput.setId(examId);
        otherUserExamOutput.setCourseId(courseId);
        otherUserExamOutput.setUserId(otherUserId); // 다른 사용자의 시험
        otherUserExamOutput.setTitle("중간고사");
        otherUserExamOutput.setStatus(Status.graded);

        when(examService.findExamById(examId)).thenReturn(Optional.of(otherUserExamOutput));

        // when, then
        mockMvc.perform(get("/v1/exams/{id}/result", examId))
                .andExpect(status().isForbidden());

        verify(examService, times(1)).findExamById(examId);
        verify(examService, never()).findExamResultByExamId(examId);
    }

    @Test
    @DisplayName("코스별 시험 결과 목록 조회")
    @WithMockUser
    void getExamResultsByCourse() throws Exception {
        // given
        when(courseService.findCourseById(courseId)).thenReturn(Optional.of(testCourseOutput));
        when(examService.findExamResultsByCourseId(courseId)).thenReturn(testExamResultListOutput);

        // when, then
        mockMvc.perform(get("/v1/exams/course/{courseId}/results", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.examResults", hasSize(1)))
                .andExpect(jsonPath("$.examResults[0].id", is(examResultId.toString())))
                .andExpect(jsonPath("$.examResults[0].examId", is(examId.toString())))
                .andExpect(jsonPath("$.examResults[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.examResults[0].score", is(85.0)))
                .andExpect(jsonPath("$.examResults[0].maxScore", is(100.0)))
                .andExpect(jsonPath("$.examResults[0].feedback", is("Good job!")));

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, times(1)).findExamResultsByCourseId(courseId);
    }

    @Test
    @DisplayName("존재하지 않는 코스의 시험 결과 목록 조회 시 404 에러")
    @WithMockUser
    void getExamResultsByCourse_CourseNotFound() throws Exception {
        // given
        when(courseService.findCourseById(courseId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(get("/v1/exams/course/{courseId}/results", courseId))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, never()).findExamResultsByCourseId(courseId);
    }

    @Test
    @DisplayName("다른 사용자의 코스 시험 결과 목록 조회 시 403 에러")
    @WithMockUser
    void getExamResultsByCourse_Forbidden() throws Exception {
        // given
        UUID otherUserId = UUID.randomUUID();
        CourseOutput otherUserCourseOutput = new CourseOutput();
        otherUserCourseOutput.setId(courseId);
        otherUserCourseOutput.setUserId(otherUserId); // 다른 사용자의 코스
        otherUserCourseOutput.setName("운영체제");

        when(courseService.findCourseById(courseId)).thenReturn(Optional.of(otherUserCourseOutput));

        // when, then
        mockMvc.perform(get("/v1/exams/course/{courseId}/results", courseId))
                .andExpect(status().isForbidden());

        verify(courseService, times(1)).findCourseById(courseId);
        verify(examService, never()).findExamResultsByCourseId(courseId);
    }
}
