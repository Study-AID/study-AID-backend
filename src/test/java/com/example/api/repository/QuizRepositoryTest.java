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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class QuizRepositoryTest {
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;

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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("퀴즈 저장 및 ID로 조회 테스트")
    void saveAndFindQuizTest() {
        // Save the quiz
        quizRepository.save(testQuiz);
        entityManager.flush();
        entityManager.clear();

        // Find the quiz by ID
        Optional<Quiz> foundQuiz = quizRepository.findById(testQuiz.getId());

        // Assert that the quiz is present and matches the saved quiz
        assertTrue(foundQuiz.isPresent());
        assertTrue(foundQuiz.get().getId().equals(testQuiz.getId()));
        assertTrue(foundQuiz.get().getTitle().equals("Quiz 1"));
    }

    @Test
    @DisplayName("강의 ID로 퀴즈 목록 조회 테스트")
    void findByLectureIdTest() {
        // Save the quiz
        quizRepository.save(testQuiz);

        Quiz anotherQuiz = new Quiz();
        anotherQuiz.setId(UUID.randomUUID());
        anotherQuiz.setLecture(testLecture);
        anotherQuiz.setUser(testUser);
        anotherQuiz.setTitle("Quiz 2");
        anotherQuiz.setStatus(Status.not_started);
        anotherQuiz.setContentsGenerateAt(LocalDateTime.now());
        quizRepository.save(anotherQuiz);

        entityManager.flush();
        entityManager.clear();

        // Find quizzes by lecture ID
        List<Quiz> quizzes = quizRepository.findByLectureId(testLecture.getId());

        // Assert that the quiz is present and matches the saved quiz
        assertTrue(quizzes.size() == 2);
    }

    @Test
    @DisplayName("사용자 ID로 퀴즈 목록 조회 테스트")
    void findByUserIdTest() {
        // Save the quiz
        quizRepository.save(testQuiz);

        Quiz anotherQuiz = new Quiz();
        anotherQuiz.setId(UUID.randomUUID());
        anotherQuiz.setLecture(testLecture);
        anotherQuiz.setUser(testUser);
        anotherQuiz.setTitle("Quiz 2");
        anotherQuiz.setStatus(Status.not_started);
        anotherQuiz.setContentsGenerateAt(LocalDateTime.now());
        quizRepository.save(anotherQuiz);

        entityManager.flush();
        entityManager.clear();

        // Find quizzes by user ID
        List<Quiz> quizzes = quizRepository.findByUserId(testUser.getId());

        // Assert that the quiz is present and matches the saved quiz
        assertTrue(quizzes.size() == 2);
    }

    @Test
    @DisplayName("퀴즈 생성 테스트")
    void createQuizTest() {
        // Create a new quiz
        Quiz createdQuiz = quizRepository.createQuiz(testQuiz);

        assertThat(createdQuiz).isNotNull();
        assertThat(createdQuiz.getId()).isNotNull();
        
        Quiz foundQuiz = entityManager.find(Quiz.class, createdQuiz.getId());
        assertThat(foundQuiz).isNotNull();
        assertThat(foundQuiz.getTitle()).isEqualTo("Quiz 1");
    }

    @Test
    @DisplayName("중복 퀴즈 생성 시 예외 발생 테스트")
    void createDuplicateQuizTest() {
        // Save the quiz
        quizRepository.save(testQuiz);

        entityManager.flush();
        entityManager.clear();

        // Attempt to create a duplicate quiz
        Quiz duplicateQuiz = new Quiz();
        duplicateQuiz.setId(testQuiz.getId());
        duplicateQuiz.setLecture(entityManager.find(Lecture.class, testLecture.getId()));
        duplicateQuiz.setUser(entityManager.find(User.class, testUser.getId()));
        duplicateQuiz.setTitle("Quiz 1");
        duplicateQuiz.setStatus(Status.not_started);
        duplicateQuiz.setContentsGenerateAt(LocalDateTime.now());
        
        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            quizRepository.createQuiz(duplicateQuiz);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("퀴즈 업데이트 테스트")
    void updateQuizTest() {
        // Save the quiz
        quizRepository.save(testQuiz);

        entityManager.flush();
        entityManager.clear();

        // Update the quiz
        testQuiz.setTitle("Updated Quiz");
        Quiz updatedQuiz = quizRepository.updateQuiz(testQuiz);

        assertThat(updatedQuiz).isNotNull();
        assertThat(updatedQuiz.getTitle()).isEqualTo("Updated Quiz");

        Quiz foundQuiz = entityManager.find(Quiz.class, updatedQuiz.getId());
        assertThat(foundQuiz).isNotNull();
        assertThat(foundQuiz.getTitle()).isEqualTo("Updated Quiz");
    }

    @Test
    @DisplayName("퀴즈 삭제 테스트")
    void deleteQuizTest() {
        // Save the quiz
        quizRepository.save(testQuiz);

        entityManager.flush();
        entityManager.clear();

        // Delete the quiz
        quizRepository.deleteQuiz(testQuiz.getId());

        entityManager.flush();
        entityManager.clear();

        // Attempt to find the deleted quiz
        Quiz deletedQuiz = entityManager.find(Quiz.class, testQuiz.getId());
        assertThat(deletedQuiz).isNotNull();
        assertThat(deletedQuiz.getDeletedAt()).isNotNull();
    }
}
