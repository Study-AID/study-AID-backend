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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class QuizResponseRepositoryTest {
    @Autowired
    private QuizResponseRepository quizResponseRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizItem testQuizItem;
    private QuizResponse testQuizResponse;

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
        entityManager.persist(testQuiz);

        testQuizItem = new QuizItem();
        testQuizItem.setId(UUID.randomUUID());
        testQuizItem.setQuiz(testQuiz);
        testQuizItem.setUser(testUser);
        testQuizItem.setQuestion("오렌지는");
        testQuizItem.setQuestionType(QuestionType.short_answer);

        entityManager.persist(testQuizItem);

        testQuizResponse = new QuizResponse();
        testQuizResponse.setId(UUID.randomUUID());
        testQuizResponse.setQuiz(testQuiz);
        testQuizResponse.setQuizItem(testQuizItem);
        testQuizResponse.setUser(testUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("퀴즈 풀이 생성 및 ID로 조회 테스트")
    void testSaveAndFindById() {
        QuizResponse createdQuizResponse = quizResponseRepository.createQuizResponse(testQuizResponse);

        assertThat(createdQuizResponse).isNotNull();
        assertThat(createdQuizResponse.getId()).isNotNull();

        List<QuizResponse> quizResponses = quizResponseRepository.findByQuizId(testQuiz.getId());
        assertThat(quizResponses).isNotNull();
        assertThat(quizResponses.size()).isGreaterThan(0);
        assertThat(quizResponses.get(0).getId()).isEqualTo(createdQuizResponse.getId());        
    }
}
