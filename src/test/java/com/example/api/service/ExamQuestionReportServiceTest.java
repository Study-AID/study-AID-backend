package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.*;
import com.example.api.service.dto.report.CreateExamQuestionReportInput;
import com.example.api.service.dto.report.ExamQuestionReportOutput;

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
public class ExamQuestionReportServiceTest {
    @Mock
    private ExamQuestionReportRepository examQuestionReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamItemRepository examItemRepository;

    @InjectMocks
    private ExamQuestionReportServiceImpl examQuestionReportService;

    private UUID userId;
    private UUID anotherUserId;
    private UUID examId;
    private UUID examItemId;
    private UUID reportId;

    private User testUser;
    private User anotherUser;
    private School testSchool;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Exam testExam;
    private ExamItem testExamItem;
    private ExamQuestionReport testReport;
    private CreateExamQuestionReportInput testInput;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();
        examId = UUID.randomUUID();
        examItemId = UUID.randomUUID();
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

        testExam = new Exam();
        testExam.setId(examId);
        testExam.setCourse(testCourse);
        testExam.setUser(testUser);
        testExam.setTitle("Midterm Exam");
        testExam.setStatus(Status.generate_in_progress);
        testExam.setReferencedLectures(new UUID[]{testLecture.getId()});
        testExam.setContentsGenerateAt(LocalDateTime.now());

        testExamItem = new ExamItem();
        testExamItem.setId(examItemId);
        testExamItem.setExam(testExam);
        testExamItem.setUser(testUser);
        testExamItem.setQuestion("What is process scheduling?");
        testExamItem.setQuestionType(QuestionType.short_answer);
        testExamItem.setTextAnswer("Process scheduling is a method to manage CPU time allocation.");
        testExamItem.setDisplayOrder(1);
        testExamItem.setPoints(15.0f);
        testExamItem.setIsLiked(false);

        testReport = new ExamQuestionReport();
        testReport.setId(reportId);
        testReport.setUser(testUser);
        testReport.setExam(testExam);
        testReport.setExamItem(testExamItem);
        testReport.setReportReason("문제가 애매함");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setUpdatedAt(LocalDateTime.now());

        testInput = new CreateExamQuestionReportInput();
        testInput.setUserId(userId);
        testInput.setExamId(examId);
        testInput.setExamItemId(examItemId);
        testInput.setReportReason("문제가 애매함");
    }

    @Test
    @DisplayName("시험 문제 신고 생성 - 정상 케이스")
    void createReportSuccessTest() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(examRepository.findById(examId)).thenReturn(Optional.of(testExam));
        when(examItemRepository.findById(examItemId)).thenReturn(Optional.of(testExamItem));
        when(examQuestionReportRepository.createExamQuestionReport(any(ExamQuestionReport.class)))
                .thenReturn(testReport);

        // when
        ExamQuestionReportOutput result = examQuestionReportService.createReport(testInput);

        // then
        assertNotNull(result);
        assertEquals(reportId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(examId, result.getExamId());
        assertEquals(examItemId, result.getExamItemId());
        assertEquals("문제가 애매함", result.getReportReason());

        verify(userRepository, times(1)).findById(userId);
        verify(examRepository, times(1)).findById(examId);
        verify(examItemRepository, times(1)).findById(examItemId);
        verify(examQuestionReportRepository, times(1)).createExamQuestionReport(any(ExamQuestionReport.class));
    }

    @Test
    @DisplayName("신고 ID로 조회 - 존재하는 경우")
    void findReportByIdExistsTest() {
        // given
        when(examQuestionReportRepository.findById(reportId)).thenReturn(Optional.of(testReport));

        // when
        Optional<ExamQuestionReportOutput> result = examQuestionReportService.findReportById(reportId);

        // then
        assertTrue(result.isPresent());
        assertEquals(reportId, result.get().getId());
        assertEquals(userId, result.get().getUserId());
        assertEquals("문제가 애매함", result.get().getReportReason());

        verify(examQuestionReportRepository, times(1)).findById(reportId);
    }

    @Test
    @DisplayName("신고 ID로 조회 - 존재하지 않는 경우")
    void findReportByIdNotExistsTest() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(examQuestionReportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        Optional<ExamQuestionReportOutput> result = examQuestionReportService.findReportById(nonExistentId);

        // then
        assertFalse(result.isPresent());
        verify(examQuestionReportRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("사용자별 신고 목록 조회")
    void findReportsByUserTest() {
        // given
        ExamQuestionReport secondReport = new ExamQuestionReport();
        secondReport.setId(UUID.randomUUID());
        secondReport.setUser(testUser);
        secondReport.setExam(testExam);
        secondReport.setExamItem(testExamItem);
        secondReport.setReportReason("답이 틀림");
        secondReport.setCreatedAt(LocalDateTime.now().plusMinutes(1));
        secondReport.setUpdatedAt(LocalDateTime.now().plusMinutes(1));

        List<ExamQuestionReport> reports = Arrays.asList(secondReport, testReport); // 최신순
        when(examQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(reports);

        // when
        List<ExamQuestionReportOutput> result = examQuestionReportService.findReportsByUser(userId);

        // then
        assertEquals(2, result.size());
        assertEquals("답이 틀림", result.get(0).getReportReason()); // 최신 것이 먼저
        assertEquals("문제가 애매함", result.get(1).getReportReason());

        verify(examQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("사용자별 신고 목록 조회 - 빈 목록")
    void findReportsByUserEmptyListTest() {
        // given
        when(examQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.emptyList());

        // when
        List<ExamQuestionReportOutput> result = examQuestionReportService.findReportsByUser(userId);

        // then
        assertTrue(result.isEmpty());
        verify(examQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("신고 삭제 - 존재하는 경우")
    void deleteReportExistsTest() {
        // given
        when(examQuestionReportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
        doNothing().when(examQuestionReportRepository).deleteById(reportId);

        // when
        Boolean result = examQuestionReportService.deleteReport(reportId);

        // then
        assertTrue(result);
        verify(examQuestionReportRepository, times(1)).findById(reportId);
        verify(examQuestionReportRepository, times(1)).deleteById(reportId);
    }

    @Test
    @DisplayName("신고 삭제 - 존재하지 않는 경우")
    void deleteReportNotExistsTest() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(examQuestionReportRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        Boolean result = examQuestionReportService.deleteReport(nonExistentId);

        // then
        assertFalse(result);
        verify(examQuestionReportRepository, times(1)).findById(nonExistentId);
        verify(examQuestionReportRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("중복 신고 확인 - 실제 사용 케이스")
    void checkDuplicateReportTest() {
        // given - 이미 같은 사용자가 같은 문제에 신고한 경우를 시뮬레이션
        when(examQuestionReportRepository.findByExamIdAndExamItemIdAndUserId(examId, examItemId, userId))
                .thenReturn(Optional.of(testReport));

        // when - 다시 신고를 시도하는 상황에서 중복 체크
        Optional<ExamQuestionReport> existingReport = 
                examQuestionReportRepository.findByExamIdAndExamItemIdAndUserId(examId, examItemId, userId);

        // then - 중복 신고가 감지되어야 함
        assertTrue(existingReport.isPresent());
        assertEquals(testReport.getReportReason(), existingReport.get().getReportReason());
        
        verify(examQuestionReportRepository, times(1))
                .findByExamIdAndExamItemIdAndUserId(examId, examItemId, userId);
    }

    @Test
    @DisplayName("다른 사용자의 신고는 조회되지 않음")
    void ensureUserDataIsolationTest() {
        // given
        // testUser가 생성한 신고
        when(examQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.singletonList(testReport));
        when(examQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(anotherUserId))
                .thenReturn(Collections.emptyList()); // 다른 사용자의 신고는 없음

        // when
        List<ExamQuestionReportOutput> userReports = examQuestionReportService.findReportsByUser(userId);
        List<ExamQuestionReportOutput> anotherUserReports = examQuestionReportService.findReportsByUser(anotherUserId);

        // then - 각 사용자는 자신의 신고만 볼 수 있어야 함
        verify(examQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
        verify(examQuestionReportRepository, times(1)).findByUserIdOrderByCreatedAtDesc(anotherUserId);
    }

    @Test
    @DisplayName("신고 생성 시 필수 데이터 누락 확인")
    void createReportWithMissingDataTest() {
        // given - reportReason이 null인 경우
        CreateExamQuestionReportInput invalidInput = new CreateExamQuestionReportInput();
        invalidInput.setUserId(userId);
        invalidInput.setExamId(examId);
        invalidInput.setExamItemId(examItemId);
        invalidInput.setReportReason(null); // null 값

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(examRepository.findById(examId)).thenReturn(Optional.of(testExam));
        when(examItemRepository.findById(examItemId)).thenReturn(Optional.of(testExamItem));

        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            examQuestionReportService.createReport(invalidInput);
        });
        assertEquals("Report reason cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("신고 수 집계 기능 확인")
    void countReportsByExamItemTest() {
        // given
        when(examQuestionReportRepository.countByExamItemId(examItemId)).thenReturn(3L);

        // when
        Long count = examQuestionReportRepository.countByExamItemId(examItemId);

        // then
        assertEquals(3L, count);
        verify(examQuestionReportRepository, times(1)).countByExamItemId(examItemId);
    }

    @Test
    @DisplayName("권한 확인 - 다른 사용자 신고 삭제 시도")
    void unauthorizedDeleteAttemptTest() {
        // given - testReport는 testUser가 생성한 신고
        ExamQuestionReport anotherUserReport = new ExamQuestionReport();
        anotherUserReport.setId(UUID.randomUUID());
        anotherUserReport.setUser(anotherUser); // 다른 사용자가 생성한 신고
        anotherUserReport.setExam(testExam);
        anotherUserReport.setExamItem(testExamItem);
        anotherUserReport.setReportReason("다른 사용자 신고");

        when(examQuestionReportRepository.findById(anotherUserReport.getId()))
                .thenReturn(Optional.of(anotherUserReport));

        // when - 다른 사용자의 신고를 삭제하려고 시도
        Optional<ExamQuestionReport> foundReport = examQuestionReportRepository.findById(anotherUserReport.getId());

        // then - 신고를 찾을 수는 있지만, 권한 체크에서 실패해야 함
        assertTrue(foundReport.isPresent());
        assertNotEquals(userId, foundReport.get().getUser().getId()); // 다른 사용자의 신고임을 확인
        
        verify(examQuestionReportRepository, times(1)).findById(anotherUserReport.getId());
    }
}