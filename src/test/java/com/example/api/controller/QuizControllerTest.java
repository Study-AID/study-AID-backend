package com.example.api.controller;

import com.example.api.adapters.sqs.SQSClient;
import com.example.api.config.TestSecurityConfig;
import com.example.api.controller.dto.quiz.CreateQuizRequest;
import com.example.api.controller.dto.quiz.SubmitQuizItem;
import com.example.api.controller.dto.quiz.SubmitQuizRequest;
import com.example.api.controller.dto.quiz.UpdateQuizRequest;
import com.example.api.entity.Quiz;
import com.example.api.entity.QuizItem;
import com.example.api.entity.User;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtAuthenticationFilter;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.*;
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
import java.time.LocalDateTime;

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
    private UUID quizResultId;

    private SemesterOutput testSemesterOutput;
    private CourseOutput testCourseOutput;
    private LectureOutput testLectureOutput;
    private QuizOutput testQuizOutput;
    private QuizResultOutput testQuizResultOutput;
    private QuizResultListOutput testQuizResultListOutput;

    private Quiz testQuiz;
    private User testUser;
    private List<QuizItem> testQuizItems;
    private QuizResponseListOutput testQuizResponseListOutput;

    @BeforeEach
    public void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        semesterId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lectureId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        quizResultId = UUID.randomUUID();

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

        // Create test entities for proper QuizItem setup
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testQuiz = new Quiz();
        testQuiz.setId(quizId);
        testQuiz.setUser(testUser);
        testQuiz.setTitle("Quiz 1");
        testQuiz.setStatus(Status.not_started);

        testQuizItems = List.of(
                new QuizItem(),
                new QuizItem());
        testQuizItems.get(0).setId(UUID.randomUUID());
        testQuizItems.get(0).setQuiz(testQuiz);
        testQuizItems.get(0).setUser(testUser);
        testQuizItems.get(0).setQuestion("Sample question 1");

        testQuizItems.get(1).setId(UUID.randomUUID());
        testQuizItems.get(1).setQuiz(testQuiz);
        testQuizItems.get(1).setUser(testUser);
        testQuizItems.get(1).setQuestion("Sample question 2");
        testQuizOutput.setQuizItems(testQuizItems);

        testQuizResponseListOutput = new QuizResponseListOutput();
        testQuizResponseListOutput.setQuizResponseOutputs(List.of(new QuizResponseOutput()));
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setId(UUID.randomUUID());
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setQuizId(quizId);
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setUserId(userId);
        testQuizResponseListOutput.getQuizResponseOutputs().get(0).setQuizItemId(testQuizItems.get(0).getId());

        testQuizResultOutput = new QuizResultOutput();
        testQuizResultOutput.setId(quizResultId);
        testQuizResultOutput.setQuizId(quizId);
        testQuizResultOutput.setUserId(userId);
        testQuizResultOutput.setScore(85.0f);
        testQuizResultOutput.setMaxScore(100.0f);
        testQuizResultOutput.setFeedback("Good job!");
        testQuizResultOutput.setStartTime(LocalDateTime.now().minusHours(1));
        testQuizResultOutput.setEndTime(LocalDateTime.now());

        testQuizResultListOutput = new QuizResultListOutput();
        testQuizResultListOutput.setQuizResults(List.of(testQuizResultOutput));
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
                .andExpect(jsonPath("$.quizItems[0].id", is(testQuizItems.get(0).getId().toString())))
                .andExpect(jsonPath("$.quizItems[0].question", is("Sample question 1")));

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
        verify(quizService, times(1)).updateQuiz(argThat(input ->
                input.getId().equals(quizId)
                        && input.getTitle().equals("Updated Quiz Title")
        ));
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
    void submitAndGradeQuiz() throws Exception {
        // given
        SubmitQuizRequest submitQuizRequest = new SubmitQuizRequest();

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));
        when(quizService.submitAndGradeQuizWithStatus(Mockito.<List<CreateQuizResponseInput>>any())).thenReturn(testQuizResponseListOutput);


        submitQuizRequest.setSubmitQuizItems(List.of(
                new SubmitQuizItem(),
                new SubmitQuizItem()));
        submitQuizRequest.getSubmitQuizItems().get(0).setQuizItemId(testQuizItems.get(0).getId());
        submitQuizRequest.getSubmitQuizItems().get(0).setQuestionType(QuestionType.true_or_false);
        submitQuizRequest.getSubmitQuizItems().get(0).setSelectedBool(true);
        submitQuizRequest.getSubmitQuizItems().get(1).setQuizItemId(testQuizItems.get(1).getId());
        submitQuizRequest.getSubmitQuizItems().get(1).setQuestionType(QuestionType.multiple_choice);
        submitQuizRequest.getSubmitQuizItems().get(1).setSelectedIndices(new Integer[] { 0, 1 });

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
    @DisplayName("퀴즈 ID로 퀴즈 결과 조회")
    @WithMockUser
    void getQuizResultById() throws Exception {
        // given
        QuizOutput gradedQuizOutput = new QuizOutput();
        gradedQuizOutput.setId(quizId);
        gradedQuizOutput.setLectureId(lectureId);
        gradedQuizOutput.setUserId(userId);
        gradedQuizOutput.setTitle("Quiz 1");
        gradedQuizOutput.setStatus(Status.graded); // 채점 완료 상태

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(gradedQuizOutput));
        when(quizService.findQuizResultByQuizId(quizId)).thenReturn(Optional.of(testQuizResultOutput));

        // when, then
        mockMvc.perform(get("/v1/quizzes/{id}/result", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(quizResultId.toString())))
                .andExpect(jsonPath("$.quizId", is(quizId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.score", is(85.0)))
                .andExpect(jsonPath("$.maxScore", is(100.0)))
                .andExpect(jsonPath("$.feedback", is("Good job!")));

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).findQuizResultByQuizId(quizId);
    }

    @Test
    @DisplayName("아직 채점되지 않은 퀴즈의 결과 조회 시 400 에러")
    @WithMockUser
    void getQuizResultById_NotGradedYet() throws Exception {
        // given
        QuizOutput notGradedQuizOutput = new QuizOutput();
        notGradedQuizOutput.setId(quizId);
        notGradedQuizOutput.setLectureId(lectureId);
        notGradedQuizOutput.setUserId(userId);
        notGradedQuizOutput.setTitle("Quiz 1");
        notGradedQuizOutput.setStatus(Status.not_started); // 제출됨 상태 (채점 전)

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(notGradedQuizOutput));

        // when, then
        mockMvc.perform(get("/v1/quizzes/{id}/result", quizId))
                .andExpect(status().isBadRequest());

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, never()).findQuizResultByQuizId(quizId);
    }

    @Test
    @DisplayName("존재하지 않는 퀴즈의 결과 조회 시 404 에러")
    @WithMockUser
    void getQuizResultById_QuizNotFound() throws Exception {
        // given
        when(quizService.findQuizById(quizId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(get("/v1/quizzes/{id}/result", quizId))
                .andExpect(status().isNotFound());

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, never()).findQuizResultByQuizId(quizId);
    }

    @Test
    @DisplayName("partially_graded 퀴즈 결과 조회 (서술형 포함)")
    @WithMockUser
    void getQuizResultById_PartiallyGradedWithEssay() throws Exception {
        // given
        QuizOutput partiallyGradedQuizOutput = new QuizOutput();
        partiallyGradedQuizOutput.setId(quizId);
        partiallyGradedQuizOutput.setLectureId(lectureId);
        partiallyGradedQuizOutput.setUserId(userId);
        partiallyGradedQuizOutput.setTitle("Quiz 1");
        partiallyGradedQuizOutput.setStatus(Status.partially_graded);

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(partiallyGradedQuizOutput));
        when(quizService.findQuizResultByQuizId(quizId)).thenReturn(Optional.of(testQuizResultOutput));

        // when, then
        mockMvc.perform(get("/v1/quizzes/{id}/result", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(quizResultId.toString())))
                .andExpect(jsonPath("$.quizId", is(quizId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.score", is(85.0)))
                .andExpect(jsonPath("$.maxScore", is(100.0)))
                .andExpect(jsonPath("$.feedback", is("Good job!")));

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).findQuizResultByQuizId(quizId);
    }

    @Test
    @DisplayName("강의별 좋아요한 퀴즈 문제 조회")
    @WithMockUser
    void getLikedQuizItemsByLecture() throws Exception {
        // given
        UUID quizItemId1 = UUID.randomUUID();
        UUID quizItemId2 = UUID.randomUUID();
        
        QuizItemOutput likedQuizItem1 = new QuizItemOutput();
        likedQuizItem1.setId(quizItemId1);
        likedQuizItem1.setQuizId(quizId);
        likedQuizItem1.setUserId(userId);
        likedQuizItem1.setQuestion("What is JVM?");
        likedQuizItem1.setQuestionType(QuestionType.short_answer);
        likedQuizItem1.setIsLiked(true);
        
        QuizItemOutput likedQuizItem2 = new QuizItemOutput();
        likedQuizItem2.setId(quizItemId2);
        likedQuizItem2.setQuizId(quizId);
        likedQuizItem2.setUserId(userId);
        likedQuizItem2.setQuestion("Is Java platform independent?");
        likedQuizItem2.setQuestionType(QuestionType.true_or_false);
        likedQuizItem2.setIsLiked(true);

        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.of(testLectureOutput));
        when(quizService.findLikedQuizItemByLectureId(lectureId))
                .thenReturn(new QuizItemListOutput(List.of(likedQuizItem1, likedQuizItem2)));

        // when, then
        mockMvc.perform(get("/v1/quizzes/lecture/{lectureId}/items/liked", lectureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizItems", hasSize(2)))
                .andExpect(jsonPath("$.quizItems[0].id", is(quizItemId1.toString())))
                .andExpect(jsonPath("$.quizItems[0].question", is("What is JVM?")))
                .andExpect(jsonPath("$.quizItems[0].questionType", is("short_answer")))
                .andExpect(jsonPath("$.quizItems[0].isLiked", is(true)))
                .andExpect(jsonPath("$.quizItems[1].id", is(quizItemId2.toString())))
                .andExpect(jsonPath("$.quizItems[1].question", is("Is Java platform independent?")))
                .andExpect(jsonPath("$.quizItems[1].questionType", is("true_or_false")))
                .andExpect(jsonPath("$.quizItems[1].isLiked", is(true)));

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, times(1)).findLikedQuizItemByLectureId(lectureId);
    }

    @Test
    @DisplayName("좋아요한 퀴즈 문제 조회 - 강의가 존재하지 않는 경우")
    @WithMockUser
    void getLikedQuizItemsByLecture_LectureNotFound() throws Exception {
        // given
        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(get("/v1/quizzes/lecture/{lectureId}/items/liked", lectureId))
                .andExpect(status().isNotFound());

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, never()).findLikedQuizItemByLectureId(any(UUID.class));
    }

    @Test
    @DisplayName("좋아요한 퀴즈 문제 조회 - 권한이 없는 경우")
    @WithMockUser
    void getLikedQuizItemsByLecture_Forbidden() throws Exception {
        // given
        LectureOutput otherUserLecture = new LectureOutput();
        otherUserLecture.setId(lectureId);
        otherUserLecture.setUserId(UUID.randomUUID()); // 다른 사용자의 강의
        otherUserLecture.setCourseId(courseId);
        otherUserLecture.setTitle("Other User's Lecture");

        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.of(otherUserLecture));

        // when, then
        mockMvc.perform(get("/v1/quizzes/lecture/{lectureId}/items/liked", lectureId))
                .andExpect(status().isForbidden());

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, never()).findLikedQuizItemByLectureId(any(UUID.class));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 좋아요 추가")
    @WithMockUser
    void toggleLikeQuizItem_AddLike() throws Exception {
        // given
        UUID quizItemId = UUID.randomUUID();
        
        QuizItemOutput toggledQuizItem = new QuizItemOutput();
        toggledQuizItem.setId(quizItemId);
        toggledQuizItem.setQuizId(quizId);
        toggledQuizItem.setUserId(userId);
        toggledQuizItem.setQuestion("What is polymorphism?");
        toggledQuizItem.setQuestionType(QuestionType.short_answer);
        toggledQuizItem.setIsLiked(true); // 토글 후 좋아요 상태

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));
        when(quizService.toggleLikeQuizItem(any(ToggleLikeQuizItemInput.class))).thenReturn(toggledQuizItem);

        // when, then
        mockMvc.perform(post("/v1/quizzes/{id}/items/{quizItemId}/toggle-like", quizId, quizItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(quizItemId.toString())))
                .andExpect(jsonPath("$.question", is("What is polymorphism?")))
                .andExpect(jsonPath("$.questionType", is("short_answer")))
                .andExpect(jsonPath("$.isLiked", is(true)));

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).toggleLikeQuizItem(argThat(input -> 
                input.getQuizId().equals(quizId) && 
                input.getQuizItemId().equals(quizItemId) && 
                input.getUserId().equals(userId)));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 좋아요 제거")
    @WithMockUser
    void toggleLikeQuizItem_RemoveLike() throws Exception {
        // given
        UUID quizItemId = UUID.randomUUID();
        
        QuizItemOutput toggledQuizItem = new QuizItemOutput();
        toggledQuizItem.setId(quizItemId);
        toggledQuizItem.setQuizId(quizId);
        toggledQuizItem.setUserId(userId);
        toggledQuizItem.setQuestion("What is encapsulation?");
        toggledQuizItem.setQuestionType(QuestionType.multiple_choice);
        toggledQuizItem.setIsLiked(false); // 토글 후 좋아요 해제 상태

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));
        when(quizService.toggleLikeQuizItem(any(ToggleLikeQuizItemInput.class))).thenReturn(toggledQuizItem);

        // when, then
        mockMvc.perform(post("/v1/quizzes/{id}/items/{quizItemId}/toggle-like", quizId, quizItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(quizItemId.toString())))
                .andExpect(jsonPath("$.question", is("What is encapsulation?")))
                .andExpect(jsonPath("$.questionType", is("multiple_choice")))
                .andExpect(jsonPath("$.isLiked", is(false)));

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).toggleLikeQuizItem(any(ToggleLikeQuizItemInput.class));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 퀴즈가 존재하지 않는 경우")
    @WithMockUser
    void toggleLikeQuizItem_QuizNotFound() throws Exception {
        // given
        UUID quizItemId = UUID.randomUUID();

        when(quizService.findQuizById(quizId)).thenReturn(Optional.empty());

        // when, then
        mockMvc.perform(post("/v1/quizzes/{id}/items/{quizItemId}/toggle-like", quizId, quizItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, never()).toggleLikeQuizItem(any(ToggleLikeQuizItemInput.class));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 권한이 없는 경우")
    @WithMockUser
    void toggleLikeQuizItem_Forbidden() throws Exception {
        // given
        UUID quizItemId = UUID.randomUUID();
        
        QuizOutput otherUserQuiz = new QuizOutput();
        otherUserQuiz.setId(quizId);
        otherUserQuiz.setLectureId(lectureId);
        otherUserQuiz.setUserId(UUID.randomUUID()); // 다른 사용자의 퀴즈
        otherUserQuiz.setTitle("Other User's Quiz");

        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(otherUserQuiz));

        // when, then
        mockMvc.perform(post("/v1/quizzes/{id}/items/{quizItemId}/toggle-like", quizId, quizItemId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, never()).toggleLikeQuizItem(any(ToggleLikeQuizItemInput.class));
    }
}