package com.example.api.controller;

import com.example.api.controller.dto.quiz.CreateQuizRequest;
import com.example.api.controller.dto.quiz.UpdateQuizRequest;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.UserRepository;
import com.example.api.security.jwt.JwtProvider;
import com.example.api.service.CourseService;
import com.example.api.service.LectureService;
import com.example.api.service.QuizService;
import com.example.api.service.SemesterService;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.lecture.LectureOutput;
import com.example.api.service.dto.quiz.*;
import com.example.api.service.dto.semester.SemesterOutput;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(
        controllers = QuizController.class,
        excludeAutoConfiguration = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class
        }
)
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


    private UUID userId;
    private UUID semesterId;
    private UUID courseId;
    private UUID lectureId;
    private UUID quizId;

    private SemesterOutput testSemesterOutput;
    private CourseOutput testCourseOutput;
    private LectureOutput testLectureOutput;
    private QuizOutput testQuizOutput;

    @BeforeEach
    public void setUp() {
        // TODO(yoon): use @WithMockUser or @WithSecurityContext instead of hard-coding userId
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
    }
    
    @Test
    @DisplayName("강의별 퀴즈 목록 조회")
    void getQuizzesByLecture() throws Exception {
        // given
        when(lectureService.findLectureById(lectureId)).thenReturn(Optional.of(testLectureOutput));
        when(quizService.findQuizzesByLectureId(lectureId)).thenReturn(new QuizListOutput(List.of(testQuizOutput)));

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
    @DisplayName("퀴즈 생성")
    void createQuiz() throws Exception {
        // given
        CreateQuizRequest createQuizRequest = new CreateQuizRequest();
        createQuizRequest.setLectureId(lectureId);
        System.out.println("createQuizRequest = " + createQuizRequest);
        System.out.println("lectureId = " + lectureId);

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
                .andExpect(jsonPath("$.title", is("Quiz 1")))
                .andExpect(jsonPath("$.status", is("not_started")));

        verify(lectureService, times(1)).findLectureById(lectureId);
        verify(quizService, times(1)).createQuiz(any(CreateQuizInput.class));
    }

    @Test
    @DisplayName("퀴즈 수정")
    void updateQuiz() throws Exception {
        // given
        UpdateQuizRequest updateQuizRequest = new UpdateQuizRequest();
        updateQuizRequest.setTitle("Updated Quiz Title");
        updateQuizRequest.setStatus(Status.not_started);

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
                .andExpect(jsonPath("$.title", is("Quiz 1")))
                .andExpect(jsonPath("$.status", is("not_started")));

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).updateQuiz(argThat(input -> 
                input.getId().equals(quizId)    
                && input.getTitle().equals("Updated Quiz Title")
                && input.getStatus().equals(Status.not_started)
        ));
    }

    @Test
    @DisplayName("퀴즈 삭제")
    void deleteQuiz() throws Exception {
        // given
        when(quizService.findQuizById(quizId)).thenReturn(Optional.of(testQuizOutput));

        // when, then
        mockMvc.perform(delete("/v1/quizzes/{id}", quizId))
                .andExpect(status().isNoContent());

        verify(quizService, times(1)).findQuizById(quizId);
        verify(quizService, times(1)).deleteQuiz(quizId);
    }
}
