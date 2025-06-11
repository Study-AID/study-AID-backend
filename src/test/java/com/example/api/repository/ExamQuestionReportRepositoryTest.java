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
public class ExamQuestionReportRepositoryTest {
    @Autowired
    private ExamQuestionReportRepository examQuestionReportRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User anotherUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Exam testExam;
    private ExamItem testExamItem;
    private ExamQuestionReport testReport;

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

        testExam = new Exam();
        testExam.setId(UUID.randomUUID());
        testExam.setCourse(testCourse);
        testExam.setUser(testUser);
        testExam.setTitle("Midterm Exam");
        testExam.setStatus(Status.generate_in_progress);
        testExam.setReferencedLectures(new UUID[]{testLecture.getId()});
        testExam.setContentsGenerateAt(LocalDateTime.now());
        entityManager.persist(testExam);

        testExamItem = new ExamItem();
        testExamItem.setId(UUID.randomUUID());
        testExamItem.setExam(testExam);
        testExamItem.setUser(testUser);
        testExamItem.setQuestion("What is process scheduling?");
        testExamItem.setQuestionType(QuestionType.short_answer);
        testExamItem.setTextAnswer("Process scheduling is a method to manage CPU time allocation.");
        testExamItem.setDisplayOrder(1);
        testExamItem.setPoints(15.0f);
        testExamItem.setIsLiked(false);
        entityManager.persist(testExamItem);

        testReport = new ExamQuestionReport();
        testReport.setId(UUID.randomUUID());
        testReport.setUser(testUser);
        testReport.setExam(testExam);
        testReport.setExamItem(testExamItem);
        testReport.setReportReason("문제가 애매함");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("시험 문제 신고 생성 테스트")
    void createExamQuestionReportTest() {
        ExamQuestionReport createdReport = examQuestionReportRepository.createExamQuestionReport(testReport);

        assertThat(createdReport).isNotNull();
        assertThat(createdReport.getId()).isNotNull();
        assertThat(createdReport.getReportReason()).isEqualTo("문제가 애매함");
        assertThat(createdReport.getCreatedAt()).isNotNull();
        assertThat(createdReport.getUpdatedAt()).isNotNull();
        assertThat(createdReport.getDeletedAt()).isNull();

        ExamQuestionReport foundReport = entityManager.find(ExamQuestionReport.class, createdReport.getId());
        assertThat(foundReport).isNotNull();
        assertThat(foundReport.getReportReason()).isEqualTo("문제가 애매함");
    }

    @Test
    @DisplayName("사용자별 시험 문제 신고 목록 조회 테스트")
    void findByUserIdOrderByCreatedAtDescTest() {
        // 첫 번째 신고 생성
        examQuestionReportRepository.createExamQuestionReport(testReport);
        
        // 두 번째 신고 생성 (나중에 생성됨)
        ExamQuestionReport secondReport = new ExamQuestionReport();
        secondReport.setId(UUID.randomUUID());
        secondReport.setUser(testUser);
        secondReport.setExam(testExam);
        secondReport.setExamItem(testExamItem);
        secondReport.setReportReason("답이 틀림");
        examQuestionReportRepository.createExamQuestionReport(secondReport);

        // 다른 사용자의 신고 생성 (결과에 포함되면 안 됨)
        ExamQuestionReport otherUserReport = new ExamQuestionReport();
        otherUserReport.setId(UUID.randomUUID());
        otherUserReport.setUser(anotherUser);
        otherUserReport.setExam(testExam);
        otherUserReport.setExamItem(testExamItem);
        otherUserReport.setReportReason("다른 사용자 신고");
        examQuestionReportRepository.createExamQuestionReport(otherUserReport);

        entityManager.flush();
        entityManager.clear();

        List<ExamQuestionReport> reports = examQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).getReportReason()).isEqualTo("답이 틀림"); // 나중에 생성된 신고가 먼저
        assertThat(reports.get(1).getReportReason()).isEqualTo("문제가 애매함");
    }

    @Test
    @DisplayName("시험 문제 신고 ID로 조회 테스트")
    void findByIdTest() {
        ExamQuestionReport createdReport = examQuestionReportRepository.createExamQuestionReport(testReport);
        entityManager.flush();
        entityManager.clear();

        Optional<ExamQuestionReport> foundReport = examQuestionReportRepository.findById(createdReport.getId());

        assertTrue(foundReport.isPresent());
        assertThat(foundReport.get().getId()).isEqualTo(createdReport.getId());
        assertThat(foundReport.get().getReportReason()).isEqualTo("문제가 애매함");
    }

    @Test
    @DisplayName("특정 시험, 시험아이템, 사용자로 신고 조회 테스트")
    void findByExamIdAndExamItemIdAndUserIdTest() {
        examQuestionReportRepository.createExamQuestionReport(testReport);
        entityManager.flush();
        entityManager.clear();

        Optional<ExamQuestionReport> foundReport = examQuestionReportRepository
                .findByExamIdAndExamItemIdAndUserId(testExam.getId(), testExamItem.getId(), testUser.getId());

        assertTrue(foundReport.isPresent());
        assertThat(foundReport.get().getReportReason()).isEqualTo("문제가 애매함");
    }

    @Test
    @DisplayName("시험아이템별 신고 수 집계 테스트")
    void countByExamItemIdTest() {
        // 같은 시험아이템에 대해 여러 신고 생성
        examQuestionReportRepository.createExamQuestionReport(testReport);
        
        ExamQuestionReport anotherReport = new ExamQuestionReport();
        anotherReport.setId(UUID.randomUUID());
        anotherReport.setUser(anotherUser);
        anotherReport.setExam(testExam);
        anotherReport.setExamItem(testExamItem);
        anotherReport.setReportReason("다른 이유");
        examQuestionReportRepository.createExamQuestionReport(anotherReport);

        entityManager.flush();
        entityManager.clear();

        Long count = examQuestionReportRepository.countByExamItemId(testExamItem.getId());

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("시험 문제 신고 삭제 테스트 (소프트 삭제)")
    void deleteByIdTest() {
        ExamQuestionReport createdReport = examQuestionReportRepository.createExamQuestionReport(testReport);
        entityManager.flush();
        entityManager.clear();

        examQuestionReportRepository.deleteById(createdReport.getId());
        entityManager.flush();
        entityManager.clear();

        // JPA findById는 deletedAt 필터링 없이 엔티티를 찾으므로 여전히 존재함
        ExamQuestionReport deletedReport = entityManager.find(ExamQuestionReport.class, createdReport.getId());
        assertThat(deletedReport).isNotNull();
        assertThat(deletedReport.getDeletedAt()).isNotNull();

        // 커스텀 메서드들은 deletedAt이 null인 것만 조회하므로 찾을 수 없음
        List<ExamQuestionReport> userReports = examQuestionReportRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertThat(userReports).isEmpty();

        Long count = examQuestionReportRepository.countByExamItemId(testExamItem.getId());
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("존재하지 않는 신고 삭제 시 예외 없이 처리되는지 테스트")
    void deleteNonExistingReportTest() {
        UUID nonExistingId = UUID.randomUUID();
        
        // 예외가 발생하지 않아야 함
        examQuestionReportRepository.deleteById(nonExistingId);
        entityManager.flush();
    }
}