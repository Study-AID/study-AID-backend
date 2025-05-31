package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.*;
import com.example.api.service.dto.quiz.CreateQuizInput;
import com.example.api.service.dto.quiz.QuizItemListOutput;
import com.example.api.service.dto.quiz.QuizItemOutput;
import com.example.api.service.dto.quiz.QuizListOutput;
import com.example.api.service.dto.quiz.QuizOutput;
import com.example.api.service.dto.quiz.ToggleLikeQuizItemInput;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class QuizServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizItemRepository quizItemRepository;

    @Mock
    private QuizResponseRepository quizResponseRepository;

    @Mock
    private QuizResultRepository quizResultRepository;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Captor
    private ArgumentCaptor<QuizResponse> quizResponseCaptor;

    @Captor
    private ArgumentCaptor<Quiz> quizCaptor;

    @Captor
    private ArgumentCaptor<QuizResult> quizResultCaptor;
    
    @Captor
    private ArgumentCaptor<LikedQuizItem> likedQuizItemCaptor;

    private UUID userId;
    private UUID courseId;
    private UUID semesterId;
    private UUID lectureId;
    private UUID quizId;
    private UUID quizItemId1;
    private UUID quizItemId2;
    private UUID quizItemId3;
    private UUID quizItemId4;

    private UUID likedQuizItemId1;
    private UUID likedQuizItemId2;
    private UUID anotherQuizId;

    private User testUser;
    private Course testCourse;
    private Semester testSemester;
    private Lecture testLecture;
    private Quiz testQuiz;
    private List<QuizItem> testQuizItems;
    private QuizOutput testQuizOutput;
    private List<QuizResponse> testQuizResponses;
    private QuizItem testQuizItem;
    
    private Quiz anotherQuiz;
    private QuizItem likedQuizItem1;
    private QuizItem likedQuizItem2;
    private QuizItem notLikedQuizItem;

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
        quizItemId4 = UUID.randomUUID(); // liked quiz item
        

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
        
        testQuizItem = new QuizItem();
        testQuizItem.setId(quizItemId4);
        testQuizItem.setQuiz(testQuiz);
        testQuizItem.setQuestionType(QuestionType.multiple_choice);
        testQuizItem.setQuestion("Test Question");

        likedQuizItemId1 = UUID.randomUUID();
        likedQuizItemId2 = UUID.randomUUID();
        anotherQuizId = UUID.randomUUID();

        anotherQuiz = new Quiz();
        anotherQuiz.setId(anotherQuizId);
        anotherQuiz.setUser(testUser);
        anotherQuiz.setLecture(testLecture);
        anotherQuiz.setTitle("Another Quiz");
        anotherQuiz.setStatus(Status.not_started);

        likedQuizItem1 = new QuizItem();
        likedQuizItem1.setId(likedQuizItemId1);
        likedQuizItem1.setQuiz(testQuiz);
        likedQuizItem1.setUser(testUser);
        likedQuizItem1.setQuestion("What is inheritance?");
        likedQuizItem1.setQuestionType(QuestionType.short_answer);
        likedQuizItem1.setIsLiked(true);

        likedQuizItem2 = new QuizItem();
        likedQuizItem2.setId(likedQuizItemId2);
        likedQuizItem2.setQuiz(anotherQuiz);
        likedQuizItem2.setUser(testUser);
        likedQuizItem2.setQuestion("Is Java object-oriented?");
        likedQuizItem2.setQuestionType(QuestionType.true_or_false);
        likedQuizItem2.setIsLiked(true);

        notLikedQuizItem = new QuizItem();
        notLikedQuizItem.setId(UUID.randomUUID());
        notLikedQuizItem.setQuiz(testQuiz);
        notLikedQuizItem.setUser(testUser);
        notLikedQuizItem.setQuestion("What is abstraction?");
        notLikedQuizItem.setQuestionType(QuestionType.short_answer);
        notLikedQuizItem.setIsLiked(false);

        // testQuizItem 초기화도 setUp()에서 처리
        testQuizItem.setUser(testUser);
        testQuizItem.setIsLiked(false); // 기본값을 false로 설정
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
        when(quizItemRepository.getReferenceById(quizItemId1)).thenReturn(testQuizResponses.get(0).getQuizItem());

        // Item 2: multiple choice
        when(quizItemRepository.getReferenceById(quizItemId2)).thenReturn(testQuizResponses.get(1).getQuizItem());

        // Item 3: short answer
        when(quizItemRepository.getReferenceById(quizItemId3)).thenReturn(testQuizResponses.get(2).getQuizItem());

        when(quizRepository.updateQuiz(any(Quiz.class))).thenReturn(testQuiz);
        when(quizResponseRepository.updateQuizResponse(any(QuizResponse.class))).thenReturn(new QuizResponse());
        when(quizResultRepository.createQuizResult(any(QuizResult.class))).thenReturn(new QuizResult());

        // when
        quizService.gradeQuiz(quizId);

        // then
        verify(quizRepository, times(1)).getReferenceById(quizId);
        verify(quizResponseRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(3)).getReferenceById(any(UUID.class));

        // Verify quiz status is updated to graded
        verify(quizRepository).updateQuiz(quizCaptor.capture());
        Quiz capturedQuiz = quizCaptor.getValue();
        assertEquals(Status.graded, capturedQuiz.getStatus());

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

        when(quizRepository.updateQuiz(any(Quiz.class))).thenReturn(testQuiz);
        when(quizResponseRepository.updateQuizResponse(any(QuizResponse.class))).thenReturn(new QuizResponse());
        when(quizResultRepository.createQuizResult(any(QuizResult.class))).thenReturn(new QuizResult());

        // when
        quizService.gradeQuiz(quizId);

        // then
        verify(quizRepository, times(1)).getReferenceById(quizId);
        verify(quizResponseRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(3)).getReferenceById(any(UUID.class));

        // Verify quiz status is updated to graded
        verify(quizRepository).updateQuiz(quizCaptor.capture());
        Quiz capturedQuiz = quizCaptor.getValue();
        assertEquals(Status.graded, capturedQuiz.getStatus());

        // Verify each response is updated
        verify(quizResponseRepository, times(3)).updateQuizResponse(quizResponseCaptor.capture());
        List<QuizResponse> capturedResponses = quizResponseCaptor.getAllValues();

        // The first two responses should NOT be marked as correct, but the third one should be
        assertEquals(testQuizResponses.get(0).getId(), capturedResponses.get(0).getId());
        assertEquals(Boolean.FALSE, capturedResponses.get(0).getIsCorrect()); // Wrong answer

        assertEquals(testQuizResponses.get(1).getId(), capturedResponses.get(1).getId());
        assertEquals(Boolean.FALSE, capturedResponses.get(1).getIsCorrect()); // Wrong answer

        assertEquals(testQuizResponses.get(2).getId(), capturedResponses.get(2).getId());
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
        assertThrows(IllegalArgumentException.class, () -> quizService.gradeQuiz(quizId));

        verify(quizRepository, times(1)).getReferenceById(quizId);
        verify(quizResponseRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(1)).getReferenceById(quizItemId1);
        verify(quizResponseRepository, never()).updateQuizResponse(any(QuizResponse.class));
        verify(quizRepository, never()).updateQuiz(any(Quiz.class));
        verify(quizResultRepository, never()).createQuizResult(any(QuizResult.class));
    }

    @Test
    @DisplayName("강의별 좋아요한 퀴즈 문제 조회 - 좋아요한 문제가 있는 경우")
    void findLikedQuizItemByLectureId_WithLikedItems() {
        // given
        when(quizRepository.findByLectureId(lectureId)).thenReturn(Arrays.asList(testQuiz, anotherQuiz));
        when(quizItemRepository.findByQuizId(quizId)).thenReturn(Arrays.asList(likedQuizItem1, notLikedQuizItem));
        when(quizItemRepository.findByQuizId(anotherQuizId)).thenReturn(Arrays.asList(likedQuizItem2));

        // when
        QuizItemListOutput result = quizService.findLikedQuizItemByLectureId(lectureId);

        // then
        assertEquals(2, result.getQuizItems().size());
        
        QuizItemOutput firstLikedItem = result.getQuizItems().get(0);
        assertEquals(likedQuizItemId1, firstLikedItem.getId());
        assertEquals("What is inheritance?", firstLikedItem.getQuestion());
        assertEquals(QuestionType.short_answer, firstLikedItem.getQuestionType());
        assertEquals(Boolean.TRUE, firstLikedItem.getIsLiked());

        QuizItemOutput secondLikedItem = result.getQuizItems().get(1);
        assertEquals(likedQuizItemId2, secondLikedItem.getId());
        assertEquals("Is Java object-oriented?", secondLikedItem.getQuestion());
        assertEquals(QuestionType.true_or_false, secondLikedItem.getQuestionType());
        assertEquals(Boolean.TRUE, secondLikedItem.getIsLiked());

        verify(quizRepository, times(1)).findByLectureId(lectureId);
        verify(quizItemRepository, times(1)).findByQuizId(quizId);
        verify(quizItemRepository, times(1)).findByQuizId(anotherQuizId);
    }

    @Test
    @DisplayName("강의별 좋아요한 퀴즈 문제 조회 - 퀴즈가 없는 경우")
    void findLikedQuizItemByLectureId_NoQuizzes() {
        // given
        when(quizRepository.findByLectureId(lectureId)).thenReturn(Arrays.asList());

        // when
        QuizItemListOutput result = quizService.findLikedQuizItemByLectureId(lectureId);

        // then
        assertEquals(0, result.getQuizItems().size());
        verify(quizRepository, times(1)).findByLectureId(lectureId);
        verify(quizItemRepository, never()).findByQuizId(any(UUID.class));
    }

    @Test
    @DisplayName("강의별 좋아요한 퀴즈 문제 조회 - 좋아요한 문제가 없는 경우")
    void findLikedQuizItemByLectureId_NoLikedItems() {
        // given
        when(quizRepository.findByLectureId(lectureId)).thenReturn(Arrays.asList(testQuiz));
        when(quizItemRepository.findByQuizId(quizId)).thenReturn(Arrays.asList(notLikedQuizItem));

        // when
        QuizItemListOutput result = quizService.findLikedQuizItemByLectureId(lectureId);

        // then
        assertEquals(0, result.getQuizItems().size());
        verify(quizRepository, times(1)).findByLectureId(lectureId);
        verify(quizItemRepository, times(1)).findByQuizId(quizId);
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 좋아요 추가 (false -> true)")
    void toggleLikeQuizItem_AddLike() {
        // given
        ToggleLikeQuizItemInput input = new ToggleLikeQuizItemInput();
        input.setQuizId(quizId);
        input.setQuizItemId(quizItemId4);
        input.setUserId(userId);

        // testQuizItem의 현재 상태는 setUp()에서 false로 설정됨
        
        QuizItem updatedQuizItem = new QuizItem();
        updatedQuizItem.setId(quizItemId4);
        updatedQuizItem.setQuiz(testQuiz);
        updatedQuizItem.setUser(testUser);
        updatedQuizItem.setQuestion("Test Question");
        updatedQuizItem.setQuestionType(QuestionType.multiple_choice);
        updatedQuizItem.setIsLiked(true); // false에서 true로 변경

        when(quizItemRepository.getReferenceById(quizItemId4)).thenReturn(testQuizItem);
        when(quizItemRepository.findById(quizItemId4)).thenReturn(Optional.of(testQuizItem));
        when(quizItemRepository.updateQuizItem(any(QuizItem.class))).thenReturn(updatedQuizItem);

        // when
        QuizItemOutput result = quizService.toggleLikeQuizItem(input);

        // then
        assertEquals(quizItemId4, result.getId());
        assertEquals(quizId, result.getQuizId());
        assertEquals(userId, result.getUserId());
        assertEquals("Test Question", result.getQuestion());
        assertEquals(QuestionType.multiple_choice, result.getQuestionType());
        assertEquals(Boolean.TRUE, result.getIsLiked());

        verify(quizItemRepository, times(1)).getReferenceById(quizItemId4);
        verify(quizItemRepository, times(1)).findById(quizItemId4);
        verify(quizItemRepository, times(1)).updateQuizItem(argThat(item -> 
                item.getId().equals(quizItemId4) && Boolean.TRUE.equals(item.getIsLiked())));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 좋아요 제거 (true -> false)")
    void toggleLikeQuizItem_RemoveLike() {
        // given
        ToggleLikeQuizItemInput input = new ToggleLikeQuizItemInput();
        input.setQuizId(quizId);
        input.setQuizItemId(quizItemId4);
        input.setUserId(userId);

        testQuizItem.setIsLiked(true); // 현재 좋아요 상태를 true로 설정
        
        QuizItem updatedQuizItem = new QuizItem();
        updatedQuizItem.setId(quizItemId4);
        updatedQuizItem.setQuiz(testQuiz);
        updatedQuizItem.setUser(testUser);
        updatedQuizItem.setQuestion("Test Question");
        updatedQuizItem.setQuestionType(QuestionType.multiple_choice);
        updatedQuizItem.setIsLiked(false); // true에서 false로 변경

        when(quizItemRepository.getReferenceById(quizItemId4)).thenReturn(testQuizItem);
        when(quizItemRepository.findById(quizItemId4)).thenReturn(Optional.of(testQuizItem));
        when(quizItemRepository.updateQuizItem(any(QuizItem.class))).thenReturn(updatedQuizItem);

        // when
        QuizItemOutput result = quizService.toggleLikeQuizItem(input);

        // then
        assertEquals(quizItemId4, result.getId());
        assertEquals(quizId, result.getQuizId());
        assertEquals(userId, result.getUserId());
        assertEquals("Test Question", result.getQuestion());
        assertEquals(QuestionType.multiple_choice, result.getQuestionType());
        assertEquals(Boolean.FALSE, result.getIsLiked());

        verify(quizItemRepository, times(1)).getReferenceById(quizItemId4);
        verify(quizItemRepository, times(1)).findById(quizItemId4);
        verify(quizItemRepository, times(1)).updateQuizItem(argThat(item -> 
                item.getId().equals(quizItemId4) && Boolean.FALSE.equals(item.getIsLiked())));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 퀴즈 문제가 해당 퀴즈에 속하지 않는 경우 예외 발생")
    void toggleLikeQuizItem_QuizItemDoesNotBelongToQuiz() {
        // given
        UUID wrongQuizId = UUID.randomUUID();
        ToggleLikeQuizItemInput input = new ToggleLikeQuizItemInput();
        input.setQuizId(wrongQuizId); // 다른 퀴즈 ID
        input.setQuizItemId(quizItemId4);
        input.setUserId(userId);

        when(quizItemRepository.getReferenceById(quizItemId4)).thenReturn(testQuizItem);

        // when, then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> quizService.toggleLikeQuizItem(input));
        
        assertEquals("Quiz item does not belong to the specified quiz", exception.getMessage());

        verify(quizItemRepository, times(1)).getReferenceById(quizItemId4);
        verify(quizItemRepository, never()).findById(any(UUID.class));
        verify(quizItemRepository, never()).updateQuizItem(any(QuizItem.class));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 퀴즈 문제가 존재하지 않는 경우 예외 발생")
    void toggleLikeQuizItem_QuizItemNotFound() {
        // given
        ToggleLikeQuizItemInput input = new ToggleLikeQuizItemInput();
        input.setQuizId(quizId);
        input.setQuizItemId(quizItemId4);
        input.setUserId(userId);

        when(quizItemRepository.getReferenceById(quizItemId4)).thenReturn(testQuizItem);
        when(quizItemRepository.findById(quizItemId4)).thenReturn(Optional.empty());

        // when, then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> quizService.toggleLikeQuizItem(input));
        
        assertEquals("Quiz item not found", exception.getMessage());

        verify(quizItemRepository, times(1)).getReferenceById(quizItemId4);
        verify(quizItemRepository, times(1)).findById(quizItemId4);
        verify(quizItemRepository, never()).updateQuizItem(any(QuizItem.class));
    }

    @Test
    @DisplayName("퀴즈 문제 좋아요 토글 - 업데이트 실패 시 예외 발생")
    void toggleLikeQuizItem_UpdateFailed() {
        // given
        ToggleLikeQuizItemInput input = new ToggleLikeQuizItemInput();
        input.setQuizId(quizId);
        input.setQuizItemId(quizItemId4);
        input.setUserId(userId);

        when(quizItemRepository.getReferenceById(quizItemId4)).thenReturn(testQuizItem);
        when(quizItemRepository.findById(quizItemId4)).thenReturn(Optional.of(testQuizItem));
        when(quizItemRepository.updateQuizItem(any(QuizItem.class))).thenReturn(null); // 업데이트 실패

        // when, then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> quizService.toggleLikeQuizItem(input));
        
        assertEquals("Failed to update quiz item like status", exception.getMessage());

        verify(quizItemRepository, times(1)).getReferenceById(quizItemId4);
        verify(quizItemRepository, times(1)).findById(quizItemId4);
        verify(quizItemRepository, times(1)).updateQuizItem(any(QuizItem.class));
    }   
}
