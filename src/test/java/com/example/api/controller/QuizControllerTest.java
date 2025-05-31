package com.example.api.controller;

import com.example.api.adapters.sqs.SQSClient;
import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.quiz.CreateQuizRequest;
import com.example.api.controller.dto.quiz.SubmitQuizItem;
import com.example.api.controller.dto.quiz.SubmitQuizRequest;
import com.example.api.controller.dto.quiz.UpdateQuizRequest;
import com.example.api.entity.Quiz;
import com.example.api.entity.QuizItem;
import com.example.api.entity.QuizResponse;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.CourseService;
import com.example.api.service.LectureService;
import com.example.api.service.QuizService;
import com.example.api.service.SemesterService;
import com.example.api.service.StorageService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.lecture.LectureOutput;
import com.example.api.service.dto.quiz.*;
import com.example.api.service.dto.semester.SemesterOutput;
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

@WebMvcTest(QuizController.class)
@Import({ TestSecurityConfig.class })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class QuizControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private QuizService quizService;

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

    private UUID userId;
    private UUID semesterId;
    private UUID courseId;
    private UUID lectureId;
    private UUID quizId;

    private SemesterOutput testSemesterOutput;
    private CourseOutput testCourseOutput;
    private LectureOutput testLectureOutput;
    private QuizOutput testQuizOutput;

    // private QuizItem testQuizItem;
    private List<QuizItem> testQuizItems;
    private QuizResponseListOutput testQuizResponseListOutput;

    @BeforeEach
    public void setUp() {
        // TODO(yoon): use @WithMockUser or @WithSecurityContext instead of hard-coding
        // userId
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lectureId = UUID.randomUUID();
        quizId = UUID.randomUUID();

        testSemesterOutput = new SemesterOutput();
        testSemesterOutput.setId(semesterId);
        testSemesterOutput.setUserId(userId);
        testSemesterOutput.setName("2025 봄학기");

        testCourseOutput = new CourseOutput();
        testCourseOutput.setId(courseId);
        testCourseOutput.setUserId(userId);
        testCourseOutput.setSemesterId(semesterId);
        testCourseOutput.setName("운영체제");

        testLectureOutput = new LectureOutput();
        testLectureOutput.setId(lectureId);
        testLectureOutput.setUserId(userId);
        testLectureOutput.setCourseId(courseId);
        testLectureOutput.setTitle("Introduction to Operating Systems");
        testLectureOutput.setMaterialPath("https://example.com/lecture/1");
        testLectureOutput.setMaterialType("pdf");
        testLectureOutput.setDisplayOrderLex("1");
        testLectureOutput.setSummaryStatus(SummaryStatus.not_started);

        testQuizOutput = new QuizOutput();
        testQuizOutput.setId(quizId);
        testQuizOutput.setLectureId(lectureId);
        testQuizOutput.setUserId(userId);
        testQuizOutput.setTitle("Quiz 1");
        testQuizOutput.setStatus(Status.not_started);

        testQuizItems = List.of(
                new QuizItem(),
                new QuizItem());
        testQuizItems.get(0).setId(UUID.randomUUID());
        testQuizOutput.setQuizItems(testQuizItems);

        testQuizResponseListOutput = new QuizResponseListOutput();
        testQuizResponseListOutput.setQuizResponseOutputs(List.of(new QuizResponseOutput()));
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setId(UUID.randomUUID());
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setQuizId(quizId);
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setUserId(userId);
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setQuizItemId(testQuizItems.get(0).getId());
    }

    @Test
    @DisplayName("강의별 퀴즈 목록 조회")
    @WithMockUser
    void getQuizzesByLecture() throws Exception {
        // given
        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.of(testLectureOutput));
        when(quizService.findQuizzesByLectureId(lectureId))
                .thenReturn(new QuizListOutput(List.of(testQuizOutput)));

        // when, then
        mockMvc.perform(get("/v1/quizzes/lecture/{lectureId}", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizzes", hasSize(1)))
                .andExpect(jsonPath("$.quizzes[0].id", is(quizId.toString())))
                .andExpect(jsonPath("$.quizzes[0].lectureId", is(lectureId.toString())))
                .andExpect(jsonPath("$.quizzes[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.quizzes[0].title", is("Quiz 1")))
                .andExpect(jsonPath("$.quizzes[0].status", is("not_started")));

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, times(1)).findQuizzesByLectureId(lectureId);
    }

    @Test
    @DisplayName("퀴즈 ID로 퀴즈 조회")
    @WithMockUser
    void getQuizById() throws Exception {
        // given
        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));

        // when, then
        mockMvc.perform(get("/v1/quizzes/{id}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(quizId.toString())))
                .andExpect(jsonPath("$.lectureId", is(lectureId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.title", is("Quiz 1")))
                .andExpect(jsonPath("$.status", is("not_started")))
                .andExpect(jsonPath("$.quizItems[0].id", is(testQuizItems.get(0).getId().toString())));

        verify(quizService, times(1)).findQuizById(quizId);
    }

    @Test
    @DisplayName("퀴즈 생성")
    @WithMockUser
    void createQuiz() throws Exception {
        // given
        CreateQuizRequest createQuizRequest = new CreateQuizRequest();
        createQuizRequest.setLectureId(lectureId);
        createQuizRequest.setTitle("Quiz 1");
        createQuizRequest.setTrueOrFalseCount(2);
        createQuizRequest.setMultipleChoiceCount(3);
        createQuizRequest.setShortAnswerCount(1);
        createQuizRequest.setEssayCount(1);

        when(courseService.findCourseById(courseId)).thenReturn(Optional.of(testCourseOutput));
        when(semesterService.findSemesterById(semesterId)).thenReturn(Optional.of(testSemesterOutput));

        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.of(testLectureOutput));
        when(quizService.createQuiz(any(CreateQuizInput.class))).thenReturn(testQuizOutput);

        // when, then
        mockMvc.perform(post("/v1/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createQuizRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(quizId.toString())))
                .andExpect(jsonPath("$.lectureId", is(lectureId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.title", is("Quiz 1")));

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, times(1)).createQuiz(any(CreateQuizInput.class));
        verify(sqsClient, times(1)).sendGenerateQuizMessage(any());
    }

    @Test
    @DisplayName("퀴즈 수정")
    @WithMockUser
    void updateQuiz() throws Exception {
        // given
        UpdateQuizRequest updateQuizRequest = new UpdateQuizRequest();
        updateQuizRequest.setTitle("Updated Quiz Title");

        // QuizOutput updatedQuizOutput = new QuizOutput();

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));
        when(quizService.updateQuiz(any(UpdateQuizInput.class))).thenReturn(testQuizOutput);

        // when, then
        mockMvc.perform(put("/v1/quizzes/{id}", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateQuizRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(quizId.toString())))
                .andExpect(jsonPath("$.lectureId", is(lectureId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.title", is("Quiz 1")));

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).updateQuiz(argThat(input -> input.getId().equals(quizId)
                && input.getTitle().equals("Updated Quiz Title")));
    }

    @Test
    @DisplayName("퀴즈 삭제")
    @WithMockUser
    void deleteQuiz() throws Exception {
        // given
        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));

        // when, then
        mockMvc.perform(delete("/v1/quizzes/{id}", quizId))
                .andExpect(status().isNoContent());

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).deleteQuiz(quizId);
    }

    @Test
    @DisplayName("퀴즈 응답 생성")
    @WithMockUser
    void createQuizResponse() throws Exception {
        // given
        SubmitQuizRequest submitQuizRequest = new SubmitQuizRequest();

        Quiz testQuiz = new Quiz();
        testQuiz.setId(quizId);

        QuizResponse testQuizResponse1 = new QuizResponse();
        testQuizResponse1.setId(UUID.randomUUID());
        testQuizResponse1.setQuiz(testQuiz);

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));
        when(quizService.createQuizResponse(Mockito.<List<CreateQuizResponseInput>>any()))
                .thenReturn(testQuizResponseListOutput);

        submitQuizRequest.setSubmitQuizItems(List.of(
                new SubmitQuizItem(),
                new SubmitQuizItem()));
        submitQuizRequest.getSubmitQuizItems().get(0).setQuizItemId(testQuizItems.get(0).getId());
        submitQuizRequest.getSubmitQuizItems().get(0).setQuestionType(QuestionType.true_or_false);
        submitQuizRequest.getSubmitQuizItems().get(0).setSelectedBool(true);
        submitQuizRequest.getSubmitQuizItems().get(1).setQuizItemId(testQuizItems.get(1).getId());
        submitQuizRequest.getSubmitQuizItems().get(1).setQuestionType(QuestionType.multiple_choice);
        submitQuizRequest.getSubmitQuizItems().get(1).setSelectedIndices(new Integer[] { 0, 1 });

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));
        when(quizService.createQuizResponse(Mockito.<List<CreateQuizResponseInput>>any()))
                .thenReturn(testQuizResponseListOutput);

        // when, then
        mockMvc.perform(post("/v1/quizzes/{id}/submit", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitQuizRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submitQuizResponses[0].id",
                        is(testQuizResponseListOutput.getQuizResponseOutputs().get(0).getId()
                                .toString())))
                .andExpect(jsonPath("$.submitQuizResponses[0].quizId", is(quizId.toString())))
                .andExpect(jsonPath("$.submitQuizResponses[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$.submitQuizResponses[0].quizItemId",
                        is(testQuizItems.get(0).getId().toString())));
    }

    @Test
    @DisplayName("유효하지 않은 요청으로 퀴즈 생성")
    @WithMockUser
    void createQuiz_InvalidRequest() throws Exception {
        // given
        CreateQuizRequest createQuizRequest = new CreateQuizRequest();
        createQuizRequest.setLectureId(lectureId);
        createQuizRequest.setTitle(""); // 빈 제목
        createQuizRequest.setTrueOrFalseCount(2);
        createQuizRequest.setMultipleChoiceCount(3);
        createQuizRequest.setShortAnswerCount(1);
        createQuizRequest.setEssayCount(1);

        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.of(testLectureOutput));

        // when, then
        mockMvc.perform(post("/v1/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createQuizRequest)))
                .andExpect(status().isBadRequest());

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, never()).createQuiz(any(CreateQuizInput.class));
        verify(sqsClient, never()).sendGenerateQuizMessage(any());
    }

    @Test
    @DisplayName("존재하지 않는 강의로 퀴즈 생성")
    @WithMockUser
    void createQuiz_LectureNotFound() throws Exception {
        // given
        CreateQuizRequest createQuizRequest = new CreateQuizRequest();
        createQuizRequest.setLectureId(lectureId);
        createQuizRequest.setTitle("Quiz 1");
        createQuizRequest.setTrueOrFalseCount(2);
        createQuizRequest.setMultipleChoiceCount(3);
        createQuizRequest.setShortAnswerCount(1);
        createQuizRequest.setEssayCount(1);

        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(post("/v1/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createQuizRequest)))
                .andExpect(status().isNotFound());

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, never()).createQuiz(any(CreateQuizInput.class));
        verify(sqsClient, never()).sendGenerateQuizMessage(any());
    }
}
