package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.*;
import com.example.api.service.dto.exam.CreateExamInput;
import com.example.api.service.dto.exam.ExamItemListOutput;
import com.example.api.service.dto.exam.ExamItemOutput;
import com.example.api.service.dto.exam.ExamListOutput;
import com.example.api.service.dto.exam.ExamOutput;
import com.example.api.service.dto.exam.ExamResultListOutput;
import com.example.api.service.dto.exam.ExamResultOutput;
import com.example.api.service.dto.exam.ToggleLikeExamItemInput;
import com.example.api.service.dto.exam.UpdateExamInput;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ExamServiceTest {
    @Mock
    private UserRepository userRepo;

    @Mock
    private CourseRepository courseRepo;

    @Mock
    private ExamRepository examRepo;

    @Mock
    private ExamItemRepository examItemRepo;

    @Mock
    private ExamResponseRepository examResponseRepo;

    @Mock
    private ExamResultRepository examResultRepo;

    @InjectMocks
    private ExamServiceImpl examService;

    @Captor
    private ArgumentCaptor<ExamResponse> examResponseCaptor;

    @Captor
    private ArgumentCaptor<Exam> examCaptor;

    @Captor
    private ArgumentCaptor<ExamResult> examResultCaptor;

    private UUID userId;
    private UUID courseId;
    private UUID examId;
    private UUID examItemId1;
    private UUID examItemId2;
    private UUID examItemId3;
    private UUID examItemId4;
    private UUID examResultId;
    
    private UUID likedExamItemId1;
    private UUID likedExamItemId2;
    private UUID anotherExamId;

    private User testUser;
    private Course testCourse;
    private Exam testExam;
    private List<ExamItem> testExamItems;
    private List<ExamResponse> testExamResponses;
    private ExamOutput testExamOutput;
    private ExamResult testExamResult;
    private List<ExamResult> testExamResults;
    private ExamItem testExamItem;
    
    private Exam anotherExam;
    private ExamItem likedExamItem1;
    private ExamItem likedExamItem2;
    private ExamItem notLikedExamItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        examId = UUID.randomUUID();
        examItemId1 = UUID.randomUUID();
        examItemId2 = UUID.randomUUID();
        examItemId3 = UUID.randomUUID();
        examItemId4 = UUID.randomUUID(); // like exam item
        examResultId = UUID.randomUUID();
        
        likedExamItemId1 = UUID.randomUUID();
        likedExamItemId2 = UUID.randomUUID();
        anotherExamId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCourse = new Course();
        testCourse.setId(courseId);
        testCourse.setUser(testUser);
        testCourse.setName("운영체제");
        testCourse.setTargetGrade(4.0f);
        testCourse.setEarnedGrade(0.0f);
        testCourse.setCompletedCredits(3);
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse.setUpdatedAt(LocalDateTime.now());

        testExam = new Exam();
        testExam.setId(examId);
        testExam.setUser(testUser);
        testExam.setCourse(testCourse);
        testExam.setTitle("중간고사");
        testExam.setStatus(Status.not_started);
        testExam.setCreatedAt(LocalDateTime.now());
        testExam.setUpdatedAt(LocalDateTime.now());
        testExam.setReferencedLectures(new UUID[] { UUID.randomUUID() });

        // Create basic exam items for basic tests
        ExamItem basicItem1 = new ExamItem();
        basicItem1.setId(UUID.randomUUID());
        basicItem1.setExam(testExam);

        ExamItem basicItem2 = new ExamItem();
        basicItem2.setId(UUID.randomUUID());
        basicItem2.setExam(testExam);

        testExamItems = Arrays.asList(basicItem1, basicItem2);
        testExamOutput = ExamOutput.fromEntity(testExam, testExamItems);

        // Create exam items for different question types for grade tests
        ExamItem trueOrFalseItem = new ExamItem();
        trueOrFalseItem.setId(examItemId1);
        trueOrFalseItem.setExam(testExam);
        trueOrFalseItem.setQuestionType(QuestionType.true_or_false);
        trueOrFalseItem.setQuestion("Is Java a static typed language?");
        trueOrFalseItem.setIsTrueAnswer(true);

        ExamItem multipleChoiceItem = new ExamItem();
        multipleChoiceItem.setId(examItemId2);
        multipleChoiceItem.setExam(testExam);
        multipleChoiceItem.setQuestionType(QuestionType.multiple_choice);
        multipleChoiceItem.setQuestion("Which of these are JVM languages?");
        multipleChoiceItem.setChoices(new String[] { "Java", "Kotlin", "C++", "Python" });
        multipleChoiceItem.setAnswerIndices(new Integer[] { 0, 1 });

        ExamItem shortAnswerItem = new ExamItem();
        shortAnswerItem.setId(examItemId3);
        shortAnswerItem.setExam(testExam);
        shortAnswerItem.setQuestionType(QuestionType.short_answer);
        shortAnswerItem.setQuestion("What does JVM stand for?");
        shortAnswerItem.setTextAnswer("Java Virtual Machine");

        // Create exam responses
        ExamResponse trueOrFalseResponse = new ExamResponse();
        trueOrFalseResponse.setId(UUID.randomUUID());
        trueOrFalseResponse.setExam(testExam);
        trueOrFalseResponse.setExamItem(trueOrFalseItem);
        trueOrFalseResponse.setUser(testUser);
        trueOrFalseResponse.setSelectedBool(true); // Correct answer
        trueOrFalseResponse.setCreatedAt(LocalDateTime.now());

        ExamResponse multipleChoiceResponse = new ExamResponse();
        multipleChoiceResponse.setId(UUID.randomUUID());
        multipleChoiceResponse.setExam(testExam);
        multipleChoiceResponse.setExamItem(multipleChoiceItem);
        multipleChoiceResponse.setUser(testUser);
        multipleChoiceResponse.setSelectedIndices(new Integer[] { 0, 1 }); // Correct answer
        multipleChoiceResponse.setCreatedAt(LocalDateTime.now());

        ExamResponse shortAnswerResponse = new ExamResponse();
        shortAnswerResponse.setId(UUID.randomUUID());
        shortAnswerResponse.setExam(testExam);
        shortAnswerResponse.setExamItem(shortAnswerItem);
        shortAnswerResponse.setUser(testUser);
        shortAnswerResponse.setTextAnswer("Java Virtual Machine"); // Correct answer
        shortAnswerResponse.setCreatedAt(LocalDateTime.now());

        testExamResponses = Arrays.asList(trueOrFalseResponse, multipleChoiceResponse, shortAnswerResponse);

        testExamResult = new ExamResult();
        testExamResult.setId(examResultId);
        testExamResult.setExam(testExam);
        testExamResult.setUser(testUser);
        testExamResult.setScore(85.0f);
        testExamResult.setMaxScore(100.0f);
        testExamResult.setFeedback("Good job!");
        testExamResult.setStartTime(LocalDateTime.now().minusHours(1));
        testExamResult.setEndTime(LocalDateTime.now());
        testExamResult.setCreatedAt(LocalDateTime.now());
        testExamResult.setUpdatedAt(LocalDateTime.now());

        testExamResults = List.of(testExamResult);

        testExamItem = new ExamItem();
        testExamItem.setId(examItemId4);
        testExamItem.setExam(testExam);
        testExamItem.setUser(testUser);
        testExamItem.setQuestionType(QuestionType.true_or_false);
        testExamItem.setQuestion("Is this a liked exam item?");
        testExamItem.setIsLiked(false);

        anotherExam = new Exam();
        anotherExam.setId(anotherExamId);
        anotherExam.setUser(testUser);
        anotherExam.setCourse(testCourse);
        anotherExam.setTitle("Another Exam");
        anotherExam.setStatus(Status.not_started);
        anotherExam.setReferencedLectures(new UUID[]{UUID.randomUUID()});

        likedExamItem1 = new ExamItem();
        likedExamItem1.setId(likedExamItemId1);
        likedExamItem1.setExam(testExam);
        likedExamItem1.setUser(testUser);
        likedExamItem1.setQuestion("What is inheritance?");
        likedExamItem1.setQuestionType(QuestionType.short_answer);
        likedExamItem1.setIsLiked(true);

        likedExamItem2 = new ExamItem();
        likedExamItem2.setId(likedExamItemId2);
        likedExamItem2.setExam(anotherExam);
        likedExamItem2.setUser(testUser);
        likedExamItem2.setQuestion("Is Java object-oriented?");
        likedExamItem2.setQuestionType(QuestionType.true_or_false);
        likedExamItem2.setIsLiked(true);

        notLikedExamItem = new ExamItem();
        notLikedExamItem.setId(UUID.randomUUID());
        notLikedExamItem.setExam(testExam);
        notLikedExamItem.setUser(testUser);
        notLikedExamItem.setQuestion("What is abstraction?");
        notLikedExamItem.setQuestionType(QuestionType.short_answer);
        notLikedExamItem.setIsLiked(false);
    }

    @Test
    @DisplayName("ID로 시험 조회")
    void findExamByIdTest() {
        when(examRepo.findById(examId)).thenReturn(Optional.of(testExam));
        when(examItemRepo.findByExamId(examId)).thenReturn(testExamItems);

        Optional<ExamOutput> result = examService.findExamById(examId);

        assertTrue(result.isPresent());
        assertEquals(testExamOutput.getId(), result.get().getId());
        assertEquals(testExamOutput.getTitle(), result.get().getTitle());
        assertEquals(testExamOutput.getUserId(), result.get().getUserId());
        assertEquals(testExamOutput.getCourseId(), result.get().getCourseId());
        assertEquals(testExamOutput.getStatus(), result.get().getStatus());
        verify(examRepo, times(1)).findById(examId);
    }

    @Test
    @DisplayName("코스 ID로 시험 목록 조회")
    void findExamsByCourseIdTest() {
        when(examRepo.findByCourseId(courseId)).thenReturn(Arrays.asList(testExam));

        ExamListOutput result = examService.findExamsByCourseId(courseId);

        assertEquals(1, result.getExams().size());
        verify(examRepo, times(1)).findByCourseId(courseId);
    }

    @Test
    @DisplayName("시험 생성")
    void createExamTest() {
        CreateExamInput input = new CreateExamInput();
        input.setUserId(userId);
        input.setCourseId(courseId);
        input.setTitle("New Exam");
        input.setReferencedLectures(new UUID[] { UUID.randomUUID() });

        when(userRepo.getReferenceById(userId)).thenReturn(testUser);
        when(courseRepo.getReferenceById(courseId)).thenReturn(testCourse);
        when(examRepo.createExam(any(Exam.class))).thenReturn(testExam);

        ExamOutput result = examService.createExam(input);

        assertEquals(testExam.getTitle(), result.getTitle());
        verify(userRepo, times(1)).getReferenceById(userId);
        verify(courseRepo, times(1)).getReferenceById(courseId);
        verify(examRepo, times(1)).createExam(any(Exam.class));
    }

    @Test
    @DisplayName("시험 업데이트")
    void updateExamTest() {
        UpdateExamInput input = new UpdateExamInput();
        input.setId(examId);
        input.setTitle("Updated Exam");

        Exam updatedExam = new Exam();
        updatedExam.setId(examId);
        updatedExam.setUser(testUser);
        updatedExam.setCourse(testCourse);
        updatedExam.setTitle("Updated Exam");
        updatedExam.setStatus(Status.not_started);

        when(examRepo.updateExam(any(Exam.class))).thenReturn(updatedExam);
        when(examItemRepo.findByExamId(examId)).thenReturn(testExamItems);

        ExamOutput result = examService.updateExam(input);
        assertEquals(updatedExam.getTitle(), result.getTitle());
        verify(examRepo, times(1)).updateExam(any(Exam.class));
    }

    @Test
    @DisplayName("시험 삭제")
    void deleteExamTest() {
        doNothing().when(examRepo).deleteExam(examId);

        examService.deleteExam(examId);

        verify(examRepo, times(1)).deleteExam(examId);
    }

    @Test
    @DisplayName("시험 채점 테스트 - 모든 답이 정확한 경우")
    void gradeExamWithAllCorrectAnswers() {
        // given
        testExam.setStatus(Status.submitted); // 채점을 위해 상태 변경

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examResponseRepo.findByExamId(examId)).thenReturn(testExamResponses);

        List<ExamItem> examItems = Arrays.asList(
                testExamResponses.get(0).getExamItem(),
                testExamResponses.get(1).getExamItem(),
                testExamResponses.get(2).getExamItem()
        );

        when(examItemRepo.findByExamId(examId)).thenReturn(examItems);

        // Item 1: true/false
        when(examItemRepo.getReferenceById(examItemId1)).thenReturn(testExamResponses.get(0).getExamItem());

        // Item 2: multiple choice
        when(examItemRepo.getReferenceById(examItemId2)).thenReturn(testExamResponses.get(1).getExamItem());

        // Item 3: short answer
        when(examItemRepo.getReferenceById(examItemId3)).thenReturn(testExamResponses.get(2).getExamItem());

        when(examResponseRepo.updateExamResponse(any(ExamResponse.class))).thenReturn(new ExamResponse());
        when(examResultRepo.createExamResult(any(ExamResult.class))).thenReturn(new ExamResult());

        // when
        examService.gradeNonEssayQuestions(examId);

        // then
        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examResponseRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(3)).getReferenceById(any(UUID.class));

        // Verify each response is updated with correct isCorrect flag
        verify(examResponseRepo, times(3)).updateExamResponse(examResponseCaptor.capture());
        List<ExamResponse> capturedResponses = examResponseCaptor.getAllValues();

        // All responses should be marked as correct by the service
        for (ExamResponse response : capturedResponses) {
            assertEquals(Boolean.TRUE, response.getIsCorrect());
        }

        // Verify exam result is created
        verify(examResultRepo).createExamResult(any(ExamResult.class));
    }

    @Test
    @DisplayName("시험 채점 테스트 - 일부 답이 틀린 경우")
    void gradeExamWithSomeIncorrectAnswers() {
        // given
        testExam.setStatus(Status.submitted); // 채점을 위해 상태 변경

        // Modify responses to have incorrect answers
        testExamResponses.get(0).setSelectedBool(false); // Wrong answer for true/false
        testExamResponses.get(1).setSelectedIndices(new Integer[] { 0, 2 }); // Wrong answer for multiple choice

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examResponseRepo.findByExamId(examId)).thenReturn(testExamResponses);

        List<ExamItem> examItems = Arrays.asList(
                testExamResponses.get(0).getExamItem(),
                testExamResponses.get(1).getExamItem(),
                testExamResponses.get(2).getExamItem()
        );

        when(examItemRepo.findByExamId(examId)).thenReturn(examItems);

        // Item 1: true/false
        when(examItemRepo.getReferenceById(examItemId1)).thenReturn(testExamResponses.get(0).getExamItem());

        // Item 2: multiple choice
        when(examItemRepo.getReferenceById(examItemId2)).thenReturn(testExamResponses.get(1).getExamItem());

        // Item 3: short answer
        when(examItemRepo.getReferenceById(examItemId3)).thenReturn(testExamResponses.get(2).getExamItem());

        when(examResponseRepo.updateExamResponse(any(ExamResponse.class))).thenReturn(new ExamResponse());
        when(examResultRepo.createExamResult(any(ExamResult.class))).thenReturn(new ExamResult());

        // when
        examService.gradeNonEssayQuestions(examId);

        // then
        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examResponseRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(3)).getReferenceById(any(UUID.class));

        // Verify each response is updated
        verify(examResponseRepo, times(3)).updateExamResponse(examResponseCaptor.capture());
        List<ExamResponse> capturedResponses = examResponseCaptor.getAllValues();

        // The first two responses should NOT be marked as correct, but the third one
        // should be
        assertEquals(testExamResponses.get(0).getId(), capturedResponses.get(0).getId());
        assertNull(capturedResponses.get(0).getIsCorrect()); // Not correct

        assertEquals(testExamResponses.get(1).getId(), capturedResponses.get(1).getId());
        assertNull(capturedResponses.get(1).getIsCorrect()); // Not correct

        assertEquals(testExamResponses.get(2).getId(), capturedResponses.get(2).getId());
        assertEquals(Boolean.TRUE, capturedResponses.get(2).getIsCorrect()); // Correct

        // Verify exam result is created
        verify(examResultRepo).createExamResult(any(ExamResult.class));
    }

    @Test
    @DisplayName("시험 채점 테스트 - question type이 null인 경우 예외 처리")
    void gradeExamWithNullQuestionType() {
        // given
        testExam.setStatus(Status.submitted); // 채점을 위해 상태 변경

        // Set questionType to null
        testExamResponses.get(0).getExamItem().setQuestionType(null);

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examResponseRepo.findByExamId(examId)).thenReturn(testExamResponses);
        when(examItemRepo.getReferenceById(examItemId1)).thenReturn(testExamResponses.get(0).getExamItem());

        // when, then
        assertThrows(IllegalArgumentException.class, () -> examService.gradeNonEssayQuestions(examId));

        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examResponseRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(1)).getReferenceById(examItemId1);
        verify(examResponseRepo, never()).updateExamResponse(any(ExamResponse.class));
        verify(examResultRepo, never()).createExamResult(any(ExamResult.class));
    }

    @Test
    @DisplayName("시험 ID로 시험 결과 조회")
    void findExamResultByExamIdTest() {
        // given
        when(examResultRepo.findByExamId(examId)).thenReturn(Optional.of(testExamResult));

        // when
        Optional<ExamResultOutput> result = examService.findExamResultByExamId(examId);

        // then
        assertTrue(result.isPresent());
        assertEquals(testExamResult.getId(), result.get().getId());
        assertEquals(testExamResult.getExam().getId(), result.get().getExamId());
        assertEquals(testExamResult.getUser().getId(), result.get().getUserId());
        assertEquals(testExamResult.getScore(), result.get().getScore());
        assertEquals(testExamResult.getMaxScore(), result.get().getMaxScore());
        assertEquals(testExamResult.getFeedback(), result.get().getFeedback());
        assertEquals(testExamResult.getStartTime(), result.get().getStartTime());
        assertEquals(testExamResult.getEndTime(), result.get().getEndTime());

        verify(examResultRepo, times(1)).findByExamId(examId);
    }

    @Test
    @DisplayName("존재하지 않는 시험 ID로 시험 결과 조회")
    void findExamResultByExamIdTest_NotFound() {
        // given
        UUID nonExistentExamId = UUID.randomUUID();
        when(examResultRepo.findByExamId(nonExistentExamId)).thenReturn(Optional.empty());

        // when
        Optional<ExamResultOutput> result = examService.findExamResultByExamId(nonExistentExamId);

        // then
        assertFalse(result.isPresent());
        verify(examResultRepo, times(1)).findByExamId(nonExistentExamId);
    }

    @Test
    @DisplayName("코스 ID로 시험 결과 목록 조회")
    void findExamResultsByCourseIdTest() {
        // given
        Exam exam1 = new Exam();
        UUID exam1Id = UUID.randomUUID();
        exam1.setId(exam1Id);
        exam1.setCourse(testCourse);

        ExamResult examResult1 = new ExamResult();
        examResult1.setId(UUID.randomUUID());
        examResult1.setExam(exam1);
        examResult1.setUser(testUser);
        examResult1.setScore(85.0f);
        examResult1.setMaxScore(100.0f);
        examResult1.setFeedback("Excellent!");
        examResult1.setStartTime(LocalDateTime.now().minusHours(3));
        examResult1.setEndTime(LocalDateTime.now().minusHours(1));
        examResult1.setCreatedAt(LocalDateTime.now());
        examResult1.setUpdatedAt(LocalDateTime.now());

        Exam exam2 = new Exam();
        UUID exam2Id = UUID.randomUUID();
        exam2.setId(exam2Id);
        exam2.setCourse(testCourse);

        ExamResult examResult2 = new ExamResult();
        examResult2.setId(UUID.randomUUID());
        examResult2.setExam(exam2);
        examResult2.setUser(testUser);
        examResult2.setScore(90.0f);
        examResult2.setMaxScore(100.0f);
        examResult2.setFeedback("Excellent!");
        examResult2.setStartTime(LocalDateTime.now().minusHours(3));
        examResult2.setEndTime(LocalDateTime.now().minusHours(2));
        examResult2.setCreatedAt(LocalDateTime.now());
        examResult2.setUpdatedAt(LocalDateTime.now());

        List<Exam> exams = List.of(exam1, exam2);

        when(examRepo.findByCourseId(courseId)).thenReturn(exams);
        when(examResultRepo.findByExamId(exam1Id)).thenReturn(Optional.of(examResult1));
        when(examResultRepo.findByExamId(exam2Id)).thenReturn(Optional.of(examResult2));

        // when
        ExamResultListOutput result = examService.findExamResultsByCourseId(courseId);

        // then
        assertNotNull(result);
        assertEquals(2, result.getExamResults().size());

        // 첫 번째 결과 검증
        ExamResultOutput firstResult = result.getExamResults().get(0);
        assertEquals(examResult1.getId(), firstResult.getId());
        assertEquals(85.0f, firstResult.getScore());

        // 두 번째 결과 검증
        ExamResultOutput secondResult = result.getExamResults().get(1);
        assertEquals(examResult2.getId(), secondResult.getId());
        assertEquals(90.0f, secondResult.getScore());

        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examResultRepo, times(1)).findByExamId(exam1Id);
        verify(examResultRepo, times(1)).findByExamId(exam2Id);
    }


    @Test
    @DisplayName("코스 ID로 시험 결과 목록 조회 - 일부 시험만 결과가 있는 경우")
    void findExamResultsByCourseIdTest_PartialResults() {
        // given
        Exam examWithResult = new Exam();
        examWithResult.setId(examId);
        examWithResult.setCourse(testCourse);

        Exam examWithoutResult = new Exam();
        UUID examWithoutResultId = UUID.randomUUID();
        examWithoutResult.setId(examWithoutResultId);
        examWithoutResult.setCourse(testCourse);

        List<Exam> exams = List.of(examWithResult, examWithoutResult);

        when(examRepo.findByCourseId(courseId)).thenReturn(exams);
        when(examResultRepo.findByExamId(examId)).thenReturn(Optional.of(testExamResult));
        when(examResultRepo.findByExamId(examWithoutResultId)).thenReturn(Optional.empty());

        // when
        ExamResultListOutput result = examService.findExamResultsByCourseId(courseId);

        // then
        assertNotNull(result);
        assertEquals(1, result.getExamResults().size());

        ExamResultOutput resultOutput = result.getExamResults().get(0);
        assertEquals(testExamResult.getId(), resultOutput.getId());
        assertEquals(85.0f, resultOutput.getScore());

        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examResultRepo, times(1)).findByExamId(examId);
        verify(examResultRepo, times(1)).findByExamId(examWithoutResultId);
    }

    @Test
    @DisplayName("시험 평균 점수 계산 - 성공 케이스")
    void calculateExamAverageScoreTest_Success() {
        // given
        Exam exam1 = new Exam();
        exam1.setId(examId);

        Exam exam2 = new Exam();
        UUID exam2Id = UUID.randomUUID();
        exam2.setId(exam2Id);

        ExamResult result1 = new ExamResult();
        result1.setScore(80.0f);
        result1.setMaxScore(100.0f); // 80%

        ExamResult result2 = new ExamResult();
        result2.setScore(45.0f);
        result2.setMaxScore(50.0f); // 90%

        List<Exam> exams = List.of(exam1, exam2);

        when(examRepo.findByCourseId(courseId)).thenReturn(exams);
        when(examResultRepo.findByExamId(examId)).thenReturn(Optional.of(result1));
        when(examResultRepo.findByExamId(exam2Id)).thenReturn(Optional.of(result2));

        // when
        Float averageScore = examService.calculateExamAverageScore(courseId);

        // then
        assertEquals(85.0f, averageScore); // (80 + 90) / 2 = 85
        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examResultRepo, times(1)).findByExamId(examId);
        verify(examResultRepo, times(1)).findByExamId(exam2Id);
    }

    @Test
    @DisplayName("시험 평균 점수 계산 - 결과가 없는 경우")
    void calculateExamAverageScoreTest_NoResults() {
        // given
        Exam exam1 = new Exam();
        exam1.setId(examId);

        List<Exam> exams = List.of(exam1);

        when(examRepo.findByCourseId(courseId)).thenReturn(exams);
        when(examResultRepo.findByExamId(examId)).thenReturn(Optional.empty());

        // when
        Float averageScore = examService.calculateExamAverageScore(courseId);

        // then
        assertEquals(0.0f, averageScore);
        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examResultRepo, times(1)).findByExamId(examId);
    }

    @Test
    @DisplayName("시험 평균 점수 계산 - 시험이 없는 경우")
    void calculateExamAverageScoreTest_NoExams() {
        // given
        when(examRepo.findByCourseId(courseId)).thenReturn(new ArrayList<>());

        // when
        Float averageScore = examService.calculateExamAverageScore(courseId);

        // then
        assertEquals(0.0f, averageScore);
        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examResultRepo, never()).findByExamId(any(UUID.class));
    }

    @Test
    @DisplayName("코스별 좋아요한 시험 문제 조회 - 좋아요한 문제가 있는 경우")
    void findLikedExamItemByCourseId_WithLikedItems() {
        // given
        when(examRepo.findByCourseId(courseId)).thenReturn(Arrays.asList(testExam, anotherExam));
        when(examItemRepo.findByExamId(examId)).thenReturn(Arrays.asList(likedExamItem1, notLikedExamItem));
        when(examItemRepo.findByExamId(anotherExamId)).thenReturn(Arrays.asList(likedExamItem2));

        // when
        ExamItemListOutput result = examService.findLikedExamItemByCourseId(courseId);

        // then
        assertEquals(2, result.getExamItems().size());
        
        ExamItemOutput firstLikedItem = result.getExamItems().get(0);
        assertEquals(likedExamItemId1, firstLikedItem.getId());
        assertEquals("What is inheritance?", firstLikedItem.getQuestion());
        assertEquals(QuestionType.short_answer, firstLikedItem.getQuestionType());
        assertEquals(Boolean.TRUE, firstLikedItem.getIsLiked());

        ExamItemOutput secondLikedItem = result.getExamItems().get(1);
        assertEquals(likedExamItemId2, secondLikedItem.getId());
        assertEquals("Is Java object-oriented?", secondLikedItem.getQuestion());
        assertEquals(QuestionType.true_or_false, secondLikedItem.getQuestionType());
        assertEquals(Boolean.TRUE, secondLikedItem.getIsLiked());

        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examItemRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(1)).findByExamId(anotherExamId);
    }

    @Test
    @DisplayName("코스별 좋아요한 시험 문제 조회 - 시험이 없는 경우")
    void findLikedExamItemByCourseId_NoExams() {
        // given
        when(examRepo.findByCourseId(courseId)).thenReturn(Arrays.asList());

        // when
        ExamItemListOutput result = examService.findLikedExamItemByCourseId(courseId);

        // then
        assertEquals(0, result.getExamItems().size());
        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examItemRepo, never()).findByExamId(any(UUID.class));
    }

    @Test
    @DisplayName("코스별 좋아요한 시험 문제 조회 - 좋아요한 문제가 없는 경우")
    void findLikedExamItemByCourseId_NoLikedItems() {
        // given
        when(examRepo.findByCourseId(courseId)).thenReturn(Arrays.asList(testExam));
        when(examItemRepo.findByExamId(examId)).thenReturn(Arrays.asList(notLikedExamItem));

        // when
        ExamItemListOutput result = examService.findLikedExamItemByCourseId(courseId);

        // then
        assertEquals(0, result.getExamItems().size());
        verify(examRepo, times(1)).findByCourseId(courseId);
        verify(examItemRepo, times(1)).findByExamId(examId);
    }

    @Test
    @DisplayName("시험 문제 좋아요 토글 - 좋아요 추가 (false -> true)")
    void toggleLikeExamItem_AddLike() {
        // given
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        // testExamItem의 현재 상태는 setUp()에서 false로 설정됨
        
        ExamItem updatedExamItem = new ExamItem();
        updatedExamItem.setId(examItemId4);
        updatedExamItem.setExam(testExam);
        updatedExamItem.setUser(testUser);
        updatedExamItem.setQuestion("Is this a liked exam item?");
        updatedExamItem.setQuestionType(QuestionType.true_or_false);
        updatedExamItem.setIsLiked(true); // false에서 true로 변경

        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);
        when(examItemRepo.findById(examItemId4)).thenReturn(Optional.of(testExamItem));
        when(examItemRepo.updateExamItem(any(ExamItem.class))).thenReturn(updatedExamItem);

        // when
        ExamItemOutput result = examService.toggleLikeExamItem(input);

        // then
        assertEquals(examItemId4, result.getId());
        assertEquals(examId, result.getExamId());
        assertEquals(userId, result.getUserId());
        assertEquals("Is this a liked exam item?", result.getQuestion());
        assertEquals(QuestionType.true_or_false, result.getQuestionType());
        assertEquals(Boolean.TRUE, result.getIsLiked());

        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(examItemRepo, times(1)).findById(examItemId4);
        verify(examItemRepo, times(1)).updateExamItem(argThat(item -> 
                item.getId().equals(examItemId4) && Boolean.TRUE.equals(item.getIsLiked())));
    }

    @Test
    @DisplayName("시험 문제 좋아요 토글 - 좋아요 제거 (true -> false)")
    void toggleLikeExamItem_RemoveLike() {
        // given
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        testExamItem.setIsLiked(true); // 현재 좋아요 상태를 true로 설정
        
        ExamItem updatedExamItem = new ExamItem();
        updatedExamItem.setId(examItemId4);
        updatedExamItem.setExam(testExam);
        updatedExamItem.setUser(testUser);
        updatedExamItem.setQuestion("Is this a liked exam item?");
        updatedExamItem.setQuestionType(QuestionType.true_or_false);
        updatedExamItem.setIsLiked(false); // true에서 false로 변경

        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);
        when(examItemRepo.findById(examItemId4)).thenReturn(Optional.of(testExamItem));
        when(examItemRepo.updateExamItem(any(ExamItem.class))).thenReturn(updatedExamItem);

        // when
        ExamItemOutput result = examService.toggleLikeExamItem(input);

        // then
        assertEquals(examItemId4, result.getId());
        assertEquals(examId, result.getExamId());
        assertEquals(userId, result.getUserId());
        assertEquals("Is this a liked exam item?", result.getQuestion());
        assertEquals(QuestionType.true_or_false, result.getQuestionType());
        assertEquals(Boolean.FALSE, result.getIsLiked());

        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(examItemRepo, times(1)).findById(examItemId4);
        verify(examItemRepo, times(1)).updateExamItem(argThat(item -> 
                item.getId().equals(examItemId4) && Boolean.FALSE.equals(item.getIsLiked())));
    }

    @Test
    @DisplayName("시험 문제 좋아요 토글 - 시험 문제가 해당 시험에 속하지 않는 경우 예외 발생")
    void toggleLikeExamItem_ExamItemDoesNotBelongToExam() {
        // given
        UUID wrongExamId = UUID.randomUUID();
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(wrongExamId); // 다른 시험 ID
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);

        // when, then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> examService.toggleLikeExamItem(input));
        
        assertEquals("Exam item does not belong to the specified exam", exception.getMessage());

        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(examItemRepo, never()).findById(any(UUID.class));
        verify(examItemRepo, never()).updateExamItem(any(ExamItem.class));
    }

    @Test
    @DisplayName("시험 문제 좋아요 토글 - 시험 문제가 존재하지 않는 경우 예외 발생")
    void toggleLikeExamItem_ExamItemNotFound() {
        // given
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);
        when(examItemRepo.findById(examItemId4)).thenReturn(Optional.empty());

        // when, then
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, 
                () -> examService.toggleLikeExamItem(input));
        
        assertEquals("Exam item not found", exception.getMessage());

        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(examItemRepo, times(1)).findById(examItemId4);
        verify(examItemRepo, never()).updateExamItem(any(ExamItem.class));
    }

    @Test
    @DisplayName("시험 문제 좋아요 토글 - 업데이트 실패 시 예외 발생")
    void toggleLikeExamItem_UpdateFailed() {
        // given
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);
        when(examItemRepo.findById(examItemId4)).thenReturn(Optional.of(testExamItem));
        when(examItemRepo.updateExamItem(any(ExamItem.class))).thenReturn(null); // 업데이트 실패

        // when, then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> examService.toggleLikeExamItem(input));
        
        assertEquals("Failed to update exam item like status", exception.getMessage());

        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(examItemRepo, times(1)).findById(examItemId4);
        verify(examItemRepo, times(1)).updateExamItem(any(ExamItem.class));
    }
}
