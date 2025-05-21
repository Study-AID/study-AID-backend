package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class QuizResultRepositoryTest {
    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizResult testQuizResult;

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

        testQuizResult = new QuizResult();
        testQuizResult.setId(UUID.randomUUID());
        testQuizResult.setQuiz(testQuiz);
        testQuizResult.setUser(testUser);
        // score, start/endTime 아직 미구현이지만 nullable=false이므로 기본값을 설정
        testQuizResult.setScore(10.0f);
        testQuizResult.setMaxScore(10.0f);
        testQuizResult.setStartTime(LocalDateTime.now());
        testQuizResult.setEndTime(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("퀴즈 결과 저장 및 ID로 조회 테스트")
    void testSaveAndFindById() {
        // given
        quizResultRepository.save(testQuizResult);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<QuizResult> foundQuizResult = quizResultRepository.findById(testQuizResult.getId());

        // then
        assertTrue(foundQuizResult.isPresent());
        assertTrue(foundQuizResult.get().getId().equals(testQuizResult.getId()));
    }

    @Test
    @DisplayName("강의 ID로 퀴즈 결과 목록 조회 테스트")
    void testFindByLectureId() {
        // given
        quizResultRepository.save(testQuizResult);
        entityManager.flush();
        entityManager.clear();

        // when
        var foundQuizResults = quizResultRepository.findByLectureId(testLecture.getId());

        // then
        assertTrue(foundQuizResults.size() > 0);
        assertTrue(foundQuizResults.get(0).getQuiz().getLecture().getId().equals(testLecture.getId()));
    }

    @Test
    @DisplayName("퀴즈 결과 create 테스트")
    void testCreateQuizResult() {
        // given
        QuizResult createdQuizResult = quizResultRepository.createQuizResult(testQuizResult);

        assertThat(createdQuizResult).isNotNull();
        assertThat(createdQuizResult.getId()).isNotNull();

        QuizResult foundQuizResult = quizResultRepository.findById(createdQuizResult.getId()).orElse(null);
        assertThat(foundQuizResult).isNotNull();
        assertThat(foundQuizResult.getId()).isEqualTo(createdQuizResult.getId());
    }
}
