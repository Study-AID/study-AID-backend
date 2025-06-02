package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.*;
import com.example.api.service.dto.quiz.CreateQuizInput;
import com.example.api.service.dto.quiz.QuizListOutput;
import com.example.api.service.dto.quiz.QuizOutput;
import com.example.api.service.dto.quiz.QuizResultListOutput;
import com.example.api.service.dto.quiz.QuizResultOutput;
import com.example.api.service.dto.quiz.UpdateQuizInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class QuizServiceTest {
    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizItemRepository quizItemRepository;

    @Mock
    private QuizResponseRepository quizResponseRepository;

    @Mock
    private QuizResultRepository quizResultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LectureRepository lectureRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Captor
    private ArgumentCaptor<QuizResponse> quizResponseCaptor;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    @Captor
    private ArgumentCaptor<QuizResult> quizResultCaptor;

    private UUID userId;
    private UUID courseId;
    private UUID semesterId;
    private UUID lectureId;
    private UUID quizId;
    private UUID quizItemId1;
    private UUID quizItemId2;
    private UUID quizItemId3;
    private UUID quizResultId;

    private User testUser;
    private Course testCourse;
    private Semester testSemester;
    private Lecture testLecture;
    private Quiz testQuiz;
    private List<QuizItem> testQuizItems;
    private QuizOutput testQuizOutput;
    private List<QuizResponse> testQuizResponses;
    private QuizResult testQuizResult;
    private List<QuizResult> testQuizResults;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        semesterId = UUID.randomUUID();
        lectureId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        quizItemId1 = UUID.randomUUID();
        quizItemId2 = UUID.randomUUID();
        quizItemId3 = UUID.randomUUID();
        quizResultId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);

        testSemester = new Semester();
        testSemester.setId(semesterId);
        testSemester.setUser(testUser);
        testSemester.setName("2025 Spring Semester");

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

        testLecture = new Lecture();
        testLecture.setId(lectureId);
        testLecture.setUser(testUser);
        testLecture.setCourse(testCourse);
        testLecture.setTitle("Introduction to Operating Systems");
        testLecture.setMaterialPath("path/to/material");
        testLecture.setMaterialType("pdf");
        testLecture.setDisplayOrderLex("1");
        testLecture.setNote(Map.of("key", "value"));
        testLecture.setSummary(Map.of("summaryKey", "summaryValue"));
        testLecture.setSummaryStatus(SummaryStatus.not_started);
        testLecture.setCreatedAt(LocalDateTime.now());
        testLecture.setUpdatedAt(LocalDateTime.now());

        testQuiz = new Quiz();
        testQuiz.setId(quizId);
        testQuiz.setUser(testUser);
        testQuiz.setLecture(testLecture);
        testQuiz.setTitle("Quiz 1");
        testQuiz.setStatus(Status.not_started);
        testQuiz.setCreatedAt(LocalDateTime.now());
        testQuiz.setUpdatedAt(LocalDateTime.now());

        // Create basic quiz items for basic tests
        QuizItem basicItem1 = new QuizItem();
        basicItem1.setId(UUID.randomUUID());
        basicItem1.setQuiz(testQuiz);

        QuizItem basicItem2 = new QuizItem();
        basicItem2.setId(UUID.randomUUID());
        basicItem2.setQuiz(testQuiz);

        testQuizItems = Arrays.asList(basicItem1, basicItem2);
        testQuizOutput = QuizOutput.fromEntity(testQuiz, testQuizItems);

        // Create quiz items for different question types for grade tests
        QuizItem trueOrFalseItem = new QuizItem();
        trueOrFalseItem.setId(quizItemId1);
        trueOrFalseItem.setQuiz(testQuiz);
        trueOrFalseItem.setQuestionType(QuestionType.true_or_false);
        trueOrFalseItem.setQuestion("Is Java a static typed language?");
        trueOrFalseItem.setIsTrueAnswer(true);

        QuizItem multipleChoiceItem = new QuizItem();
        multipleChoiceItem.setId(quizItemId2);
        multipleChoiceItem.setQuiz(testQuiz);
        multipleChoiceItem.setQuestionType(QuestionType.multiple_choice);
        multipleChoiceItem.setQuestion("Which of these are JVM languages?");
        multipleChoiceItem.setChoices(new String[]{"Java", "Kotlin", "C++", "Python"});
        multipleChoiceItem.setAnswerIndices(new Integer[]{0, 1});

        QuizItem shortAnswerItem = new QuizItem();
        shortAnswerItem.setId(quizItemId3);
        shortAnswerItem.setQuiz(testQuiz);
        shortAnswerItem.setQuestionType(QuestionType.short_answer);
        shortAnswerItem.setQuestion("What does JVM stand for?");
        shortAnswerItem.setTextAnswer("Java Virtual Machine");

        testQuizItems = Arrays.asList(trueOrFalseItem, multipleChoiceItem, shortAnswerItem);

        // Create quiz responses
        QuizResponse trueOrFalseResponse = new QuizResponse();
        trueOrFalseResponse.setId(UUID.randomUUID());
        trueOrFalseResponse.setQuiz(testQuiz);
        trueOrFalseResponse.setQuizItem(trueOrFalseItem);
        trueOrFalseResponse.setUser(testUser);
        trueOrFalseResponse.setSelectedBool(true); // Correct answer
        trueOrFalseResponse.setCreatedAt(LocalDateTime.now());

        QuizResponse multipleChoiceResponse = new QuizResponse();
        multipleChoiceResponse.setId(UUID.randomUUID());
        multipleChoiceResponse.setQuiz(testQuiz);
        multipleChoiceResponse.setQuizItem(multipleChoiceItem);
        multipleChoiceResponse.setUser(testUser);
        multipleChoiceResponse.setSelectedIndices(new Integer[]{0, 1}); // Correct answer
        multipleChoiceResponse.setCreatedAt(LocalDateTime.now());

        QuizResponse shortAnswerResponse = new QuizResponse();
        shortAnswerResponse.setId(UUID.randomUUID());
        shortAnswerResponse.setQuiz(testQuiz);
        shortAnswerResponse.setQuizItem(shortAnswerItem);
        shortAnswerResponse.setUser(testUser);
        shortAnswerResponse.setTextAnswer("Java Virtual Machine"); // Correct answer
        shortAnswerResponse.setCreatedAt(LocalDateTime.now());

        testQuizResponses = Arrays.asList(trueOrFalseResponse, multipleChoiceResponse, shortAnswerResponse);

        testQuizResult = new QuizResult();
        testQuizResult.setId(quizResultId);
        testQuizResult.setQuiz(testQuiz);
        testQuizResult.setUser(testUser);
        testQuizResult.setScore(85.0f);
        testQuizResult.setMaxScore(100.0f);
        testQuizResult.setFeedback("Good job!");
        testQuizResult.setStartTime(LocalDateTime.now().minusHours(1));
        testQuizResult.setEndTime(LocalDateTime.now());
        testQuizResult.setCreatedAt(LocalDateTime.now());
        testQuizResult.setUpdatedAt(LocalDateTime.now());

        testQuizResults = List.of(testQuizResult);
    }

    @Test
    @DisplayName("ID로 퀴즈 조회")
    void findQuizByIdTest() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
        when(quizItemRepository.findByQuizId(quizId)).thenReturn(testQuizItems);

        Optional<QuizOutput> result = quizService.findQuizById(quizId);

        assertTrue(result.isPresent());
        assertEquals(testQuizOutput, result.get());
        assertEquals(result.get().getQuizItems().get(0).getQuiz(), testQuiz);
        verify(quizRepository, times(1)).findById(quizId);
    }

    @Test
    @DisplayName("강의 ID로 퀴즈 목록 조회")
    void findQuizzesByLectureIdTest() {
        when(quizRepository.findByLectureId(lectureId)).thenReturn(Arrays.asList(testQuiz));

        QuizListOutput result = quizService.findQuizzesByLectureId(lectureId);

        assertEquals(1, result.getQuizzes().size());
        verify(quizRepository, times(1)).findByLectureId(lectureId);
    }

    @Test
    @DisplayName("퀴즈 생성")
    void createQuizTest() {
        CreateQuizInput input = new CreateQuizInput();
        input.setUserId(userId);
        input.setLectureId(lectureId);
        input.setTitle("New Quiz");

        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(lectureRepository.getReferenceById(lectureId)).thenReturn(testLecture);
        when(quizRepository.createQuiz(any(Quiz.class))).thenReturn(testQuiz);

        QuizOutput result = quizService.createQuiz(input);

        assertEquals(testQuiz.getTitle(), result.getTitle());
        verify(userRepository, times(1)).getReferenceById(userId);
        verify(lectureRepository, times(1)).getReferenceById(lectureId);
        verify(quizRepository, times(1)).createQuiz(any(Quiz.class));
    }

    @Test
    @DisplayName("퀴즈 업데이트")
    void updateQuizTest() {
        UpdateQuizInput input = new UpdateQuizInput();
        input.setId(quizId);
        input.setTitle("Updated Quiz");

        Quiz updatedQuiz = new Quiz();
        updatedQuiz.setId(quizId);
        updatedQuiz.setUser(testUser);
        updatedQuiz.setLecture(testLecture);
        updatedQuiz.setTitle("Updated Quiz");
        updatedQuiz.setStatus(Status.not_started);

        when(quizRepository.updateQuiz(any(Quiz.class))).thenReturn(updatedQuiz);
        when(quizItemRepository.findByQuizId(quizId)).thenReturn(testQuizItems);

        QuizOutput result = quizService.updateQuiz(input);
        assertEquals(updatedQuiz.getTitle(), result.getTitle());
        verify(quizRepository, times(1)).updateQuiz(any(Quiz.class));
    }

    @Test
    @DisplayName("퀴즈 삭제")
    void deleteQuizTest() {
        doNothing().when(quizRepository).deleteQuiz(quizId);

        quizService.deleteQuiz(quizId);

        verify(quizRepository, times(1)).deleteQuiz(quizId);
    }

    @Test
    @DisplayName("퀴즈 채점 테스트 - 모든 답이 정확한 경우")
    void gradeQuizWithAllCorrectAnswers() {
        // given
        testQuiz.setStatus(Status.submitted); // 채점을 위해 상태 변경

        when(quizRepository.getReferenceById(quizId)).thenReturn(testQuiz);
        when(quizResponseRepository.findByQuizId(quizId)).thenReturn(testQuizResponses);

        // Item 1: true/false
        when(quizItemRepository.getReferenceById(quizItemId1)).thenReturn(testQuizItems.get(0));

        // Item 2: multiple choice
        when(quizItemRepository.getReferenceById(quizItemId2)).thenReturn(testQuizItems.get(1));

        // Item 3: short answer
        when(quizItemRepository.getReferenceById(quizItemId3)).thenReturn(testQuizItems.get(2));
        // 변경: updateQuiz 제거 (gradeNonEssayQuestions는 퀴즈 상태를 직접 업데이트하지 않음)
        when(quizResponseRepository.updateQuizResponse(any(QuizResponse.class))).thenReturn(new QuizResponse());
        when(quizResultRepository.createQuizResult(any(QuizResult.class))).thenReturn(new QuizResult());

        // when
        quizService.gradeNonEssayQuestions(quizId);

        // then
        verify(quizRepository, times(1)).getReferenceById(quizId);
        verify(quizResponseRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(3)).getReferenceById(any(UUID.class));

        // 변경: 퀴즈 상태 업데이트 검증 제거 (gradeNonEssayQuestions는 퀴즈 상태를 직접 업데이트하지 않음)

        // Verify each response is updated with correct isCorrect flag
        verify(quizResponseRepository, times(3)).updateQuizResponse(quizResponseCaptor.capture());
        List<QuizResponse> capturedResponses = quizResponseCaptor.getAllValues();

        // All responses should be marked as correct by the service
        for (QuizResponse response : capturedResponses) {
            assertEquals(Boolean.TRUE, response.getIsCorrect());
        }

        // Verify quiz result is created
        verify(quizResultRepository).createQuizResult(any(QuizResult.class));
    }

    @Test
    @DisplayName("퀴즈 채점 테스트 - 일부 답이 틀린 경우")
    void gradeQuizWithSomeIncorrectAnswers() {
        // given
        testQuiz.setStatus(Status.submitted); // 채점을 위해 상태 변경

        // Modify responses to have incorrect answers
        testQuizResponses.get(0).setSelectedBool(false); // Wrong answer for true/false
        testQuizResponses.get(1).setSelectedIndices(new Integer[]{0, 2}); // Wrong answer for multiple choice

        when(quizRepository.getReferenceById(quizId)).thenReturn(testQuiz);
        when(quizResponseRepository.findByQuizId(quizId)).thenReturn(testQuizResponses);

        // Item 1: true/false
        when(quizItemRepository.getReferenceById(quizItemId1)).thenReturn(testQuizResponses.get(0).getQuizItem());

        // Item 2: multiple choice
        when(quizItemRepository.getReferenceById(quizItemId2)).thenReturn(testQuizResponses.get(1).getQuizItem());

        // Item 3: short answer
        when(quizItemRepository.getReferenceById(quizItemId3)).thenReturn(testQuizResponses.get(2).getQuizItem());

        when(quizResponseRepository.updateQuizResponse(any(QuizResponse.class))).thenReturn(new QuizResponse());
        when(quizResultRepository.createQuizResult(any(QuizResult.class))).thenReturn(new QuizResult());

        // when
        quizService.gradeNonEssayQuestions(quizId);

        // then
        verify(quizRepository, times(1)).getReferenceById(quizId);
        verify(quizResponseRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(3)).getReferenceById(any(UUID.class));

        // 변경: 퀴즈 상태 업데이트 검증 제거 (gradeNonEssayQuestions는 퀴즈 상태를 직접 업데이트하지 않음)

        // Verify each response is updated
        verify(quizResponseRepository, times(3)).updateQuizResponse(quizResponseCaptor.capture());
        List<QuizResponse> capturedResponses = quizResponseCaptor.getAllValues();

        assertEquals(Boolean.FALSE, capturedResponses.get(0).getIsCorrect()); // Wrong answer - set to false
        assertEquals(Boolean.FALSE, capturedResponses.get(1).getIsCorrect()); // Wrong answer - set to false
        assertEquals(Boolean.TRUE, capturedResponses.get(2).getIsCorrect()); // Correct

        // Verify quiz result is created
        verify(quizResultRepository).createQuizResult(any(QuizResult.class));
    }

    @Test
    @DisplayName("퀴즈 채점 테스트 - question type이 null인 경우 예외 처리")
    void gradeQuizWithNullQuestionType() {
        // given
        testQuiz.setStatus(Status.submitted); // 채점을 위해 상태 변경

        // Set questionType to null
        testQuizResponses.get(0).getQuizItem().setQuestionType(null);

        when(quizRepository.getReferenceById(quizId)).thenReturn(testQuiz);
        when(quizResponseRepository.findByQuizId(quizId)).thenReturn(testQuizResponses);
        when(quizItemRepository.getReferenceById(quizItemId1)).thenReturn(testQuizResponses.get(0).getQuizItem());

        // when, then
        assertThrows(IllegalArgumentException.class, () -> quizService.gradeNonEssayQuestions(quizId));

        verify(quizRepository, times(1)).getReferenceById(quizId);
        verify(quizResponseRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(1)).getReferenceById(quizItemId1);
        verify(quizResponseRepository, never()).updateQuizResponse(any(QuizResponse.class));
        // 변경: updateQuiz 제거 (gradeNonEssayQuestions는 퀴즈 상태를 직접 업데이트하지 않음)
        verify(quizResultRepository, never()).createQuizResult(any(QuizResult.class));
    }

    @Test
    @DisplayName("퀴즈 ID로 퀴즈 결과 조회")
    void findQuizResultByQuizIdTest() {
        // given
        when(quizResultRepository.findByQuizId(quizId)).thenReturn(Optional.of(testQuizResult));

        // when
        Optional<QuizResultOutput> result = quizService.findQuizResultByQuizId(quizId);

        // then
        assertTrue(result.isPresent());
        assertEquals(testQuizResult.getId(), result.get().getId());
        assertEquals(testQuizResult.getQuiz().getId(), result.get().getQuizId());
        assertEquals(testQuizResult.getUser().getId(), result.get().getUserId());
        assertEquals(testQuizResult.getScore(), result.get().getScore());
        assertEquals(testQuizResult.getMaxScore(), result.get().getMaxScore());
        assertEquals(testQuizResult.getFeedback(), result.get().getFeedback());
        assertEquals(testQuizResult.getStartTime(), result.get().getStartTime());
        assertEquals(testQuizResult.getEndTime(), result.get().getEndTime());

        verify(quizResultRepository, times(1)).findByQuizId(quizId);
    }

    @Test
    @DisplayName("존재하지 않는 퀴즈 ID로 퀴즈 결과 조회")
    void findQuizResultByQuizIdTest_NotFound() {
        // given
        UUID nonExistentQuizId = UUID.randomUUID();
        when(quizResultRepository.findByQuizId(nonExistentQuizId)).thenReturn(Optional.empty());

        // when
        Optional<QuizResultOutput> result = quizService.findQuizResultByQuizId(nonExistentQuizId);

        // then
        assertFalse(result.isPresent());
        verify(quizResultRepository, times(1)).findByQuizId(nonExistentQuizId);
    }
}