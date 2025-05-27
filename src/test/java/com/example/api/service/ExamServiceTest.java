package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.*;
import com.example.api.service.dto.exam.CreateExamInput;
import com.example.api.service.dto.exam.ExamListOutput;
import com.example.api.service.dto.exam.ExamOutput;
import com.example.api.service.dto.exam.ToggleLikeExamItemInput;
import com.example.api.service.dto.exam.ToggleLikeExamItemOutput;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private LikedExamItemRepository likedExamItemRepo;

    @InjectMocks
    private ExamServiceImpl examService;

    @Captor
    private ArgumentCaptor<ExamResponse> examResponseCaptor;

    @Captor
    private ArgumentCaptor<Exam> examCaptor;

    @Captor
    private ArgumentCaptor<ExamResult> examResultCaptor;

    @Captor
    private ArgumentCaptor<LikedExamItem> likedExamItemCaptor;

    private UUID userId;
    private UUID courseId;
    private UUID examId;
    private UUID examItemId1;
    private UUID examItemId2;
    private UUID examItemId3;
    private UUID examItemId4;

    private User testUser;
    private Course testCourse;
    private Exam testExam;
    private List<ExamItem> testExamItems;
    private ExamOutput testExamOutput;
    private List<ExamResponse> testExamResponses;
    private ExamItem testExamItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        examId = UUID.randomUUID();
        examItemId1 = UUID.randomUUID();
        examItemId2 = UUID.randomUUID();
        examItemId3 = UUID.randomUUID();
        examItemId4 = UUID.randomUUID(); // like exam item

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
        testExam.setReferencedLectures(new UUID[]{UUID.randomUUID()});

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
        multipleChoiceItem.setChoices(new String[]{"Java", "Kotlin", "C++", "Python"});
        multipleChoiceItem.setAnswerIndices(new Integer[]{0, 1});

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
        multipleChoiceResponse.setSelectedIndices(new Integer[]{0, 1}); // Correct answer
        multipleChoiceResponse.setCreatedAt(LocalDateTime.now());

        ExamResponse shortAnswerResponse = new ExamResponse();
        shortAnswerResponse.setId(UUID.randomUUID());
        shortAnswerResponse.setExam(testExam);
        shortAnswerResponse.setExamItem(shortAnswerItem);
        shortAnswerResponse.setUser(testUser);
        shortAnswerResponse.setTextAnswer("Java Virtual Machine"); // Correct answer
        shortAnswerResponse.setCreatedAt(LocalDateTime.now());

        testExamResponses = Arrays.asList(trueOrFalseResponse, multipleChoiceResponse, shortAnswerResponse);

        testExamItem = new ExamItem();
        testExamItem.setId(examItemId4);
        testExamItem.setExam(testExam);
        testExamItem.setQuestionType(QuestionType.true_or_false);
        testExamItem.setQuestion("Is this a liked exam item?");
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
        input.setReferencedLectures(new UUID[]{UUID.randomUUID()});

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

        // Item 1: true/false
        when(examItemRepo.getReferenceById(examItemId1)).thenReturn(testExamResponses.get(0).getExamItem());

        // Item 2: multiple choice
        when(examItemRepo.getReferenceById(examItemId2)).thenReturn(testExamResponses.get(1).getExamItem());

        // Item 3: short answer
        when(examItemRepo.getReferenceById(examItemId3)).thenReturn(testExamResponses.get(2).getExamItem());

        when(examRepo.updateExam(any(Exam.class))).thenReturn(testExam);
        when(examResponseRepo.updateExamResponse(any(ExamResponse.class))).thenReturn(new ExamResponse());
        when(examResultRepo.createExamResult(any(ExamResult.class))).thenReturn(new ExamResult());

        // when
        examService.gradeExam(examId);

        // then
        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examResponseRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(3)).getReferenceById(any(UUID.class));

        // Verify exam status is updated to graded
        verify(examRepo).updateExam(examCaptor.capture());
        Exam capturedExam = examCaptor.getValue();
        assertEquals(Status.graded, capturedExam.getStatus());

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
        testExamResponses.get(1).setSelectedIndices(new Integer[]{0, 2}); // Wrong answer for multiple choice

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examResponseRepo.findByExamId(examId)).thenReturn(testExamResponses);

        // Item 1: true/false
        when(examItemRepo.getReferenceById(examItemId1)).thenReturn(testExamResponses.get(0).getExamItem());

        // Item 2: multiple choice
        when(examItemRepo.getReferenceById(examItemId2)).thenReturn(testExamResponses.get(1).getExamItem());

        // Item 3: short answer
        when(examItemRepo.getReferenceById(examItemId3)).thenReturn(testExamResponses.get(2).getExamItem());

        when(examRepo.updateExam(any(Exam.class))).thenReturn(testExam);
        when(examResponseRepo.updateExamResponse(any(ExamResponse.class))).thenReturn(new ExamResponse());
        when(examResultRepo.createExamResult(any(ExamResult.class))).thenReturn(new ExamResult());

        // when
        examService.gradeExam(examId);

        // then
        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examResponseRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(3)).getReferenceById(any(UUID.class));

        // Verify exam status is updated to graded
        verify(examRepo).updateExam(examCaptor.capture());
        Exam capturedExam = examCaptor.getValue();
        assertEquals(Status.graded, capturedExam.getStatus());

        // Verify each response is updated
        verify(examResponseRepo, times(3)).updateExamResponse(examResponseCaptor.capture());
        List<ExamResponse> capturedResponses = examResponseCaptor.getAllValues();

        // The first two responses should NOT be marked as correct, but the third one should be
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
        assertThrows(IllegalArgumentException.class, () -> examService.gradeExam(examId));

        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examResponseRepo, times(1)).findByExamId(examId);
        verify(examItemRepo, times(1)).getReferenceById(examItemId1);
        verify(examResponseRepo, never()).updateExamResponse(any(ExamResponse.class));
        verify(examRepo, never()).updateExam(any(Exam.class));
        verify(examResultRepo, never()).createExamResult(any(ExamResult.class));
    }

    @Test
    @DisplayName("시험 아이템 좋아요 토글 - 좋아요 추가")
    void toggleLikeExamItemAddTest() {
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);
        when(userRepo.getReferenceById(userId)).thenReturn(testUser);
        when(likedExamItemRepo.findByExamItemIdAndUserId(examItemId4, userId)).thenReturn(Optional.empty());
        when(likedExamItemRepo.createLikedExamItem(any(LikedExamItem.class))).thenReturn(new LikedExamItem());

        ToggleLikeExamItemOutput result = examService.toggleLikeExamItem(input);

        assertNotNull(result);
        assertEquals(examId, result.getExamId());
        assertEquals(examItemId4, result.getExamItemId());
        assertEquals(userId, result.getUserId());
        assertTrue(result.isLiked());

        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(userRepo, times(1)).getReferenceById(userId);
        verify(likedExamItemRepo, times(1)).findByExamItemIdAndUserId(examItemId4, userId);
        verify(likedExamItemRepo, times(1)).createLikedExamItem(likedExamItemCaptor.capture());
        verify(likedExamItemRepo, never()).deleteLikedExamItem(any(UUID.class));
        
        // Verify the created LikedExamItem
        LikedExamItem capturedLikedExamItem = likedExamItemCaptor.getValue();
        assertEquals(testExam, capturedLikedExamItem.getExam());
        assertEquals(testExamItem, capturedLikedExamItem.getExamItem());
        assertEquals(testUser, capturedLikedExamItem.getUser());
    }

    @Test
    @DisplayName("시험 아이템 좋아요 토글 - 좋아요 제거")
    void toggleLikeExamItemRemoveTest() {
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4);
        input.setUserId(userId);

        LikedExamItem existingLikedExamItem = new LikedExamItem();
        existingLikedExamItem.setId(UUID.randomUUID());
        existingLikedExamItem.setExam(testExam);
        existingLikedExamItem.setExamItem(testExamItem);
        existingLikedExamItem.setUser(testUser);

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examItemRepo.getReferenceById(examItemId4)).thenReturn(testExamItem);
        when(userRepo.getReferenceById(userId)).thenReturn(testUser);
        when(likedExamItemRepo.findByExamItemIdAndUserId(examItemId4, userId)).thenReturn(Optional.of(existingLikedExamItem));

        ToggleLikeExamItemOutput result = examService.toggleLikeExamItem(input);

        assertNotNull(result);
        assertEquals(examId, result.getExamId());
        assertEquals(examItemId4, result.getExamItemId());
        assertEquals(userId, result.getUserId());
        assertFalse(result.isLiked());

        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examItemRepo, times(1)).getReferenceById(examItemId4);
        verify(userRepo, times(1)).getReferenceById(userId);
        verify(likedExamItemRepo, times(1)).findByExamItemIdAndUserId(examItemId4, userId);
        verify(likedExamItemRepo, times(1)).deleteLikedExamItem(existingLikedExamItem.getId());
    }

    @Test
    @DisplayName("시험 아이템 좋아요 토글 - 예외 처리 - 시험 아이템이 시험에 속하지 않는 경우")
    void toggleLikeExamItemInvalidExamItemTest() {
        ToggleLikeExamItemInput input = new ToggleLikeExamItemInput();
        input.setExamId(examId);
        input.setExamItemId(examItemId4); // 다른 시험 아이템 ID
        input.setUserId(userId);

        Exam anotherExam = new Exam();
        anotherExam.setId(UUID.randomUUID());

        ExamItem anotherExamItem = new ExamItem();
        anotherExamItem.setId(UUID.randomUUID());
        anotherExamItem.setExam(anotherExam);

        when(examRepo.getReferenceById(examId)).thenReturn(testExam);
        when(examItemRepo.getReferenceById(input.getExamItemId())).thenReturn(anotherExamItem);
        when(userRepo.getReferenceById(userId)).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () -> {
            examService.toggleLikeExamItem(input);
        });

        verify(examRepo, times(1)).getReferenceById(examId);
        verify(examItemRepo, times(1)).getReferenceById(input.getExamItemId());
        verify(userRepo, times(1)).getReferenceById(userId);
        verify(likedExamItemRepo, never()).findByExamItemIdAndUserId(any(UUID.class), any(UUID.class));
        verify(likedExamItemRepo, never()).createLikedExamItem(any(LikedExamItem.class));
        verify(likedExamItemRepo, never()).deleteLikedExamItem(any(UUID.class));
    }
}
