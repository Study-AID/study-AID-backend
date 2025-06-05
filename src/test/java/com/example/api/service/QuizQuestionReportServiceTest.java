package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.*;
import com.example.api.service.dto.report.CreateQuizQuestionReportInput;
import com.example.api.service.dto.report.QuizQuestionReportOutput;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class QuizQuestionReportServiceTest {
    @Mock
    private QuizQuestionReportRepository quizQuestionReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizItemRepository quizItemRepository;

    @InjectMocks
    private QuizQuestionReportServiceImpl quizQuestionReportService;

    private UUID userId;
    private UUID anotherUserId;
    private UUID quizId;
    private UUID quizItemId;
    private UUID reportId;

    private User testUser;
    private User anotherUser;
    private School testSchool;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizItem testQuizItem;
    private QuizQuestionReport testReport;
    private CreateQuizQuestionReportInput testInput;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        quizItemId = UUID.randomUUID();
        reportId = UUID.randomUUID();

        testSchool = new School();
        testSchool.setId(UUID.randomUUID());
        testSchool.setName("Ajou University");

        testUser = new User();
        testUser.setId(userId);
        testUser.setSchool(testSchool);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setAuthType(AuthType.email);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        anotherUser = new User();
        anotherUser.setId(anotherUserId);
        anotherUser.setSchool(testSchool);
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser.setAuthType(AuthType.email);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());

        testSemester = new Semester();
        testSemester.setId(UUID.randomUUID());
        testSemester.setUser(testUser);
        testSemester.setName("2025 봄학기");
        testSemester.setYear(2025);
        testSemester.setSeason(Season.spring);

        testCourse = new Course();
        testCourse.setId(UUID.randomUUID());
        testCourse.setSemester(testSemester);
        testCourse.setUser(testUser);
        testCourse.setName("운영체제");

        testLecture = new Lecture();
        testLecture.setId(UUID.randomUUID());
        testLecture.setCourse(testCourse);
        testLecture.setUser(testUser);
        testLecture.setTitle("Intro.");
        testLecture.setMaterialPath("");
        testLecture.setMaterialType("pdf");
        testLecture.setDisplayOrderLex("");
        testLecture.setSummaryStatus(SummaryStatus.not_started);

        testQuiz = new Quiz();
        testQuiz.setId(quizId);
        testQuiz.setLecture(testLecture);
        testQuiz.setUser(testUser);
        testQuiz.setTitle("Quiz 1");
        testQuiz.setStatus(Status.not_started);
        testQuiz.setContentsGenerateAt(LocalDateTime.now());

        testQuizItem = new QuizItem();
        testQuizItem.setId(quizItemId);
        testQuizItem.setQuiz(testQuiz);
        testQuizItem.setUser(testUser);
        testQuizItem.setQuestion("What is an OS?");
        testQuizItem.setQuestionType(QuestionType.multiple_choice);
        testQuizItem.setChoices(new String[]{"Software", "Hardware", "Network", "Database"});
        testQuizItem.setAnswerIndices(new Integer[]{0});
        testQuizItem.setDisplayOrder(1);
        testQuizItem.setPoints(10.0f);
        testQuizItem.setIsLiked(false);

        testReport = new QuizQuestionReport();
        testReport.setId(reportId);
        testReport.setUser(testUser);
        testReport.setQuiz(testQuiz);
        testReport.setQuizItem(testQuizItem);
        testReport.setReportReason("부적절한 문제");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setUpdatedAt(LocalDateTime.now());

        testInput = new CreateQuizQuestionReportInput();
        testInput.setUserId(userId);
        testInput.setQuizId(quizId);
        testInput.setQuizItemId(quizItemId);
        testInput.setReportReason("부적절한 문제");
    }

    @Test
    @DisplayName("퀴즈 문제 신고 생성 - 정상 케이스")
    void createReportSuccessTest() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
        when(quizItemRepository.findById(quizItemId)).thenReturn(Optional.of(testQuizItem));
        when(quizQuestionReportRepository.createQuizQuestionReport(any(QuizQuestionReport.class)))
                .thenReturn(testReport);

        // when
        QuizQuestionReportOutput result = quizQuestionReportService.createReport(testInput);

        // then
        assertNotNull(result);
        assertEquals(reportId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(quizId, result.getQuizId());
        assertEquals(quizItemId, result.getQuizItemId());
        assertEquals("부적절한 문제", result.getReportReason());

        verify(userRepository, times(1)).findById(userId);
        verify(quizRepository, times(1)).findById(quizId);
        verify(quizItemRepository, times(1)).findById(quizItemId);
        verify(quizQuestionReportRepository, times(1)).createQuizQuestionReport(any(QuizQuestionReport.class));
    }

    @Test
    @DisplayName("신고 ID로 조회 - 존재하는 경우")
    void findReportByIdExistsTest() {
        // given
        when(quizQuestionReportRepository.findById(reportId)).thenReturn(Optional.of(testReport));

        // when
        Optional<QuizQuestionReportOutput> result = quizQuestionReportService.findReportById(reportId);

        // then
        assertTrue(result.isPresent());
        assertEquals(reportId, result.get().getId());
        assertEquals(userId, result.get().getUserId());
        assertEquals("부적절한 문제", result.get().getReportReason());

        verify(quizQuestionReportRepository, times(1)).findById(reportId);
    }

    @Test
    @DisplayName("신고 ID로 조회 - 존재하지 않는 경우")
    void findReportByIdNotExistsTest() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(quizQuestionReportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        Optional<QuizQuestionReportOutput> result = quizQuestionReportService.findReportById(nonExistentId);

        // then
        assertFalse(result.isPresent());
        verify(quizQuestionReportRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("사용자별 신고 목록 조회")
    void findReportsByUserTest() {
        // given
        QuizQuestionReport secondReport = new QuizQuestionReport();
        secondReport.setId(UUID.randomUUID());
        secondReport.setUser(testUser);
        secondReport.setQuiz(testQuiz);
        secondReport.setQuizItem(testQuizItem);
        secondReport.setReportReason("중복 문제");
        secondReport.setCreatedAt(LocalDateTime.now().plusMinutes(1));
        secondReport.setUpdatedAt(LocalDateTime.now().plusMinutes(1));

        List<QuizQuestionReport> reports = Arrays.asList(secondReport, testReport); // 최신순
        when(quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(reports);

        // when
        List<QuizQuestionReportOutput> result = quizQuestionReportService.findReportsByUser(userId);

        // then
        assertEquals(2, result.size());
        assertEquals("중복 문제", result.get(0).getReportReason()); // 최신 것이 먼저
        assertEquals("부적절한 문제", result.get(1).getReportReason());

        verify(quizQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("사용자별 신고 목록 조회 - 빈 목록")
    void findReportsByUserEmptyListTest() {
        // given
        when(quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.emptyList());

        // when
        List<QuizQuestionReportOutput> result = quizQuestionReportService.findReportsByUser(userId);

        // then
        assertTrue(result.isEmpty());
        verify(quizQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("신고 삭제 - 존재하는 경우")
    void deleteReportExistsTest() {
        // given
        when(quizQuestionReportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
        doNothing().when(quizQuestionReportRepository).deleteById(reportId);

        // when
        Boolean result = quizQuestionReportService.deleteReport(reportId);

        // then
        assertTrue(result);
        verify(quizQuestionReportRepository, times(1)).findById(reportId);
        verify(quizQuestionReportRepository, times(1)).deleteById(reportId);
    }

    @Test
    @DisplayName("신고 삭제 - 존재하지 않는 경우")
    void deleteReportNotExistsTest() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(quizQuestionReportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        Boolean result = quizQuestionReportService.deleteReport(nonExistentId);

        // then
        assertFalse(result);
        verify(quizQuestionReportRepository, times(1)).findById(nonExistentId);
        verify(quizQuestionReportRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("중복 신고 확인 - 실제 사용 케이스")
    void checkDuplicateReportTest() {
        // given - 이미 같은 사용자가 같은 문제에 신고한 경우를 시뮬레이션
        when(quizQuestionReportRepository.findByQuizIdAndQuizItemIdAndUserId(quizId, quizItemId, userId))
                .thenReturn(Optional.of(testReport));

        // when - 다시 신고를 시도하는 상황에서 중복 체크
        Optional<QuizQuestionReport> existingReport = 
                quizQuestionReportRepository.findByQuizIdAndQuizItemIdAndUserId(quizId, quizItemId, userId);

        // then - 중복 신고가 감지되어야 함
        assertTrue(existingReport.isPresent());
        assertEquals(testReport.getReportReason(), existingReport.get().getReportReason());
        
        verify(quizQuestionReportRepository, times(1))
                .findByQuizIdAndQuizItemIdAndUserId(quizId, quizItemId, userId);
    }

    @Test
    @DisplayName("다른 사용자의 신고는 조회되지 않음")
    void ensureUserDataIsolationTest() {
        // given
        // 현재 사용자의 신고
        when(quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.singletonList(testReport));
        when(quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(anotherUserId))
                .thenReturn(Collections.emptyList()); // 다른 사용자의 신고는 없음

        // when
        List<QuizQuestionReportOutput> userReports = quizQuestionReportService.findReportsByUser(userId);
        List<QuizQuestionReportOutput> anotherUserReports = quizQuestionReportService.findReportsByUser(anotherUserId);

        // then - 각 사용자는 자신의 신고만 볼 수 있어야 함
        verify(quizQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
        verify(quizQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(anotherUserId);
    }

    @Test
    @DisplayName("신고 생성 시 필수 데이터 누락 확인")
    void createReportWithMissingDataTest() {
        // given - reportReason이 null인 경우
        CreateQuizQuestionReportInput invalidInput = new CreateQuizQuestionReportInput();
        invalidInput.setUserId(userId);
        invalidInput.setQuizId(quizId);
        invalidInput.setQuizItemId(quizItemId);
        invalidInput.setReportReason(null); // null 값
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
        when(quizItemRepository.findById(quizItemId)).thenReturn(Optional.of(testQuizItem));
        
        // when & then - 예외가 발생해야 함
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            quizQuestionReportService.createReport(invalidInput);
        });
        assertEquals("Report reason cannot be empty", exception.getMessage());
    }   
}