package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.*;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizItem testQuizItem;
    private QuizQuestionReport testQuizQuestionReport;

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
        testQuizItem.setQuestion("오렌지는");
        testQuizItem.setQuestionType(QuestionType.short_answer);

        entityManager.persist(testQuizItem);

        testQuizQuestionReport = new QuizQuestionReport();
        testQuizQuestionReport.setId(UUID.randomUUID());
        testQuizQuestionReport.setQuiz(testQuiz);
        testQuizQuestionReport.setQuizItem(testQuizItem);
        testQuizQuestionReport.setUser(testUser);
        testQuizQuestionReport.setReportReason("그냥 싫어요.");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("퀴즈 질문 신고 저장 및 ID로 조회 테스트")
    void testCreateAndFindQuizQuestionReport() {
        // Save the report
        quizQuestionReportRepository.createQuizQuestionReport(testQuizQuestionReport);

        // Find by ID
        Optional<QuizQuestionReport> foundReport = quizQuestionReportRepository.findById(testQuizQuestionReport.getId());

        // Verify the report was saved and retrieved correctly
        assertTrue(foundReport.isPresent(), "The report should be present after saving.");
        assertTrue(foundReport.get().getId().equals(testQuizQuestionReport.getId()), "The retrieved report ID should match the saved report ID.");
    }

    @Test
    @DisplayName("퀴즈 질문 신고 저장 및 퀴즈 ID, 퀴즈 아이템 ID, 사용자 ID로 조회 테스트")
    void testFindByQuizIdAndQuizItemIdAndUserId() {
        // Save the report
        quizQuestionReportRepository.createQuizQuestionReport(testQuizQuestionReport);

        // Find by quiz ID, quiz item ID, and user ID
        Optional<QuizQuestionReport> foundReport = quizQuestionReportRepository.findByQuizIdAndQuizItemIdAndUserId(
                testQuiz.getId(), testQuizItem.getId(), testUser.getId());

        // Verify the report was found correctly
        assertTrue(foundReport.isPresent(), "The report should be present when queried by quiz ID, quiz item ID, and user ID.");
        assertTrue(foundReport.get().getId().equals(testQuizQuestionReport.getId()), "The retrieved report ID should match the saved report ID.");
    }

    @Test
    @DisplayName("퀴즈 아이템 ID로 퀴즈 질문 신고 목록 조회 테스트")
    void testFindByQuizItemIdOrderByCreatedAtDesc() {
        // Save the report
        quizQuestionReportRepository.createQuizQuestionReport(testQuizQuestionReport);

        // Find by quiz item ID
        var reports = quizQuestionReportRepository.findByQuizItemIdOrderByCreatedAtDesc(testQuizItem.getId());

        // Verify the report is in the list and sorted by createdAt
        assertTrue(reports.size() > 0, "There should be at least one report for the quiz item.");
        assertTrue(reports.get(0).getId().equals(testQuizQuestionReport.getId()), "The first report should match the saved report.");
    }

    @Test
    @DisplayName("사용자 ID로 퀴즈 질문 신고 목록 조회 테스트")
    void testFindByUserIdOrderByCreatedAtDesc() {
        // Save the report
        quizQuestionReportRepository.createQuizQuestionReport(testQuizQuestionReport);

        // Find by user ID
        var reports = quizQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        // Verify the report is in the list and sorted by createdAt
        assertTrue(reports.size() > 0, "There should be at least one report for the user.");
        assertTrue(reports.get(0).getId().equals(testQuizQuestionReport.getId()), "The first report should match the saved report.");
    }

    @Test
    @DisplayName("퀴즈 아이템 ID로 퀴즈 질문 신고 개수 조회 테스트")
    void testCountByQuizItemId() {
        // Save the report
        quizQuestionReportRepository.createQuizQuestionReport(testQuizQuestionReport);

        // Save the another report for the same quiz item
        QuizQuestionReport anotherReport = new QuizQuestionReport();
        anotherReport.setId(UUID.randomUUID());
        anotherReport.setQuiz(testQuiz);
        anotherReport.setQuizItem(testQuizItem);
        anotherReport.setUser(testUser);
        anotherReport.setReportReason("Another reason.");
        quizQuestionReportRepository.createQuizQuestionReport(anotherReport);

        // Count reports by quiz item ID
        Long count = quizQuestionReportRepository.countByQuizItemId(testQuizItem.getId());

        // Verify the count is correct
        assertTrue(count == 2, "There should be at least one report for the quiz item.");
    }
}
