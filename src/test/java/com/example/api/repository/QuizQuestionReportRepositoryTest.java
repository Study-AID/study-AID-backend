package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class QuizQuestionReportRepositoryTest {
    @Autowired
    private QuizQuestionReportRepository quizQuestionReportRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User anotherUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizItem testQuizItem;
    private QuizQuestionReport testReport;

    @BeforeEach
    void setUp() {
        School testSchool = new School();
        testSchool.setId(UUID.randomUUID());
        testSchool.setName("Ajou University");
        entityManager.persist(testSchool);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setSchool(testSchool);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setAuthType(AuthType.email);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(testUser);

        anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setSchool(testSchool);
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser.setAuthType(AuthType.email);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(anotherUser);

        testSemester = new Semester();
        testSemester.setId(UUID.randomUUID());
        testSemester.setUser(testUser);
        testSemester.setName("2025 봄학기");
        testSemester.setYear(2025);
        testSemester.setSeason(Season.spring);
        entityManager.persist(testSemester);

        testCourse = new Course();
        testCourse.setId(UUID.randomUUID());
        testCourse.setSemester(testSemester);
        testCourse.setUser(testUser);
        testCourse.setName("운영체제");
        entityManager.persist(testCourse);

        testLecture = new Lecture();
        testLecture.setId(UUID.randomUUID());
        testLecture.setCourse(testCourse);
        testLecture.setUser(testUser);
        testLecture.setTitle("Intro.");
        testLecture.setMaterialPath("");
        testLecture.setMaterialType("pdf");
        testLecture.setDisplayOrderLex("");
        testLecture.setSummaryStatus(SummaryStatus.not_started);
        entityManager.persist(testLecture);

        testQuiz = new Quiz();
        testQuiz.setId(UUID.randomUUID());
        testQuiz.setLecture(testLecture);
        testQuiz.setUser(testUser);
        testQuiz.setTitle("Quiz 1");
        testQuiz.setStatus(Status.not_started);
        testQuiz.setContentsGenerateAt(LocalDateTime.now());
        entityManager.persist(testQuiz);

        testQuizItem = new QuizItem();
        testQuizItem.setId(UUID.randomUUID());
        testQuizItem.setQuiz(testQuiz);
        testQuizItem.setUser(testUser);
        testQuizItem.setQuestion("What is an OS?");
        testQuizItem.setQuestionType(QuestionType.multiple_choice);
        testQuizItem.setChoices(new String[]{"Software", "Hardware", "Network", "Database"});
        testQuizItem.setAnswerIndices(new Integer[]{0});
        testQuizItem.setDisplayOrder(1);
        testQuizItem.setPoints(10.0f);
        testQuizItem.setIsLiked(false);
        entityManager.persist(testQuizItem);

        testReport = new QuizQuestionReport();
        testReport.setId(UUID.randomUUID());
        testReport.setUser(testUser);
        testReport.setQuiz(testQuiz);
        testReport.setQuizItem(testQuizItem);
        testReport.setReportReason("부적절한 문제");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("퀴즈 문제 신고 생성 테스트")
    void createQuizQuestionReportTest() {
        QuizQuestionReport createdReport = quizQuestionReportRepository.createQuizQuestionReport(testReport);

        assertThat(createdReport).isNotNull();
        assertThat(createdReport.getId()).isNotNull();
        assertThat(createdReport.getReportReason()).isEqualTo("부적절한 문제");
        assertThat(createdReport.getCreatedAt()).isNotNull();
        assertThat(createdReport.getUpdatedAt()).isNotNull();
        assertThat(createdReport.getDeletedAt()).isNull();

        QuizQuestionReport foundReport = entityManager.find(QuizQuestionReport.class, createdReport.getId());
        assertThat(foundReport).isNotNull();
        assertThat(foundReport.getReportReason()).isEqualTo("부적절한 문제");
    }

    @Test
    @DisplayName("사용자별 퀴즈 문제 신고 목록 조회 테스트")
    void findByUserIdOrderByCreatedAtDescTest() {
        // 첫 번째 신고 생성
        quizQuestionReportRepository.createQuizQuestionReport(testReport);
        
        // 두 번째 신고 생성 (나중에 생성됨)
        QuizQuestionReport secondReport = new QuizQuestionReport();
        secondReport.setId(UUID.randomUUID());
        secondReport.setUser(testUser);
        secondReport.setQuiz(testQuiz);
        secondReport.setQuizItem(testQuizItem);
        secondReport.setReportReason("중복 문제");
        quizQuestionReportRepository.createQuizQuestionReport(secondReport);

        // 다른 사용자의 신고 생성 (결과에 포함되면 안 됨)
        QuizQuestionReport otherUserReport = new QuizQuestionReport();
        otherUserReport.setId(UUID.randomUUID());
        otherUserReport.setUser(anotherUser);
        otherUserReport.setQuiz(testQuiz);
        otherUserReport.setQuizItem(testQuizItem);
        otherUserReport.setReportReason("다른 사용자 신고");
        quizQuestionReportRepository.createQuizQuestionReport(otherUserReport);

        entityManager.flush();
        entityManager.clear();

        List<QuizQuestionReport> reports = quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).getReportReason()).isEqualTo("중복 문제"); // 나중에 생성된 신고가 먼저
        assertThat(reports.get(1).getReportReason()).isEqualTo("부적절한 문제");
    }

    @Test
    @DisplayName("퀴즈 문제 신고 ID로 조회 테스트")
    void findByIdTest() {
        QuizQuestionReport createdReport = quizQuestionReportRepository.createQuizQuestionReport(testReport);
        entityManager.flush();
        entityManager.clear();

        Optional<QuizQuestionReport> foundReport = quizQuestionReportRepository.findById(createdReport.getId());

        assertTrue(foundReport.isPresent());
        assertThat(foundReport.get().getId()).isEqualTo(createdReport.getId());
        assertThat(foundReport.get().getReportReason()).isEqualTo("부적절한 문제");
    }

    @Test
    @DisplayName("특정 퀴즈, 퀴즈아이템, 사용자로 신고 조회 테스트")
    void findByQuizIdAndQuizItemIdAndUserIdTest() {
        quizQuestionReportRepository.createQuizQuestionReport(testReport);
        entityManager.flush();
        entityManager.clear();

        Optional<QuizQuestionReport> foundReport = quizQuestionReportRepository
                .findByQuizIdAndQuizItemIdAndUserId(testQuiz.getId(), testQuizItem.getId(), testUser.getId());

        assertTrue(foundReport.isPresent());
        assertThat(foundReport.get().getReportReason()).isEqualTo("부적절한 문제");
    }

    @Test
    @DisplayName("퀴즈아이템별 신고 수 집계 테스트")
    void countByQuizItemIdTest() {
        // 같은 퀴즈아이템에 대해 여러 신고 생성
        quizQuestionReportRepository.createQuizQuestionReport(testReport);
        
        QuizQuestionReport anotherReport = new QuizQuestionReport();
        anotherReport.setId(UUID.randomUUID());
        anotherReport.setUser(anotherUser);
        anotherReport.setQuiz(testQuiz);
        anotherReport.setQuizItem(testQuizItem);
        anotherReport.setReportReason("다른 이유");
        quizQuestionReportRepository.createQuizQuestionReport(anotherReport);

        entityManager.flush();
        entityManager.clear();

        Long count = quizQuestionReportRepository.countByQuizItemId(testQuizItem.getId());

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("퀴즈 문제 신고 삭제 테스트 (소프트 삭제)")
    void deleteByIdTest() {
        QuizQuestionReport createdReport = quizQuestionReportRepository.createQuizQuestionReport(testReport);
        entityManager.flush();
        entityManager.clear();

        quizQuestionReportRepository.deleteById(createdReport.getId());
        entityManager.flush();
        entityManager.clear();

        // JPA findById는 deletedAt 필터링 없이 엔티티를 찾으므로 여전히 존재함
        QuizQuestionReport deletedReport = entityManager.find(QuizQuestionReport.class, createdReport.getId());
        assertThat(deletedReport).isNotNull();
        assertThat(deletedReport.getDeletedAt()).isNotNull();

        // 커스텀 메서드들은 deletedAt이 null인 것만 조회하므로 찾을 수 없음
        List<QuizQuestionReport> userReports = quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertThat(userReports).isEmpty();

        Long count = quizQuestionReportRepository.countByQuizItemId(testQuizItem.getId());
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("존재하지 않는 신고 삭제 시 예외 없이 처리되는지 테스트")
    void deleteNonExistingReportTest() {
        UUID nonExistingId = UUID.randomUUID();
        
        // 예외가 발생하지 않아야 함
        quizQuestionReportRepository.deleteById(nonExistingId);
        entityManager.flush();
    }
}