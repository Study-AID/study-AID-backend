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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class QuizItemRepositoryTest {
    @Autowired
    private QuizItemRepository quizItemRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizItem testQuizItem;

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
        testQuizItem.setIsLiked(false);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("퀴즈 아이템 저장 및 ID로 조회 테스트")
    void saveAndFindByIdTest() {
        // Save the quiz item
        quizItemRepository.save(testQuizItem);
        entityManager.flush();
        entityManager.clear();

        // Find by ID
        Optional<QuizItem> foundQuizItem = quizItemRepository.findById(testQuizItem.getId());
        assertTrue(foundQuizItem.isPresent(), "퀴즈 아이템이 존재해야 합니다.");
        assertTrue(foundQuizItem.get().getId().equals(testQuizItem.getId()), "저장된 퀴즈 아이템의 ID가 일치해야 합니다.");
    }

    @Test
    @DisplayName("퀴즈 아이템 저장 및 퀴즈 ID로 조회 테스트")
    void saveAndFindByQuizIdTest() {
        // Save the quiz item
        quizItemRepository.save(testQuizItem);
        entityManager.flush();
        entityManager.clear();

        // Find by quiz ID
        var foundQuizItems = quizItemRepository.findByQuizId(testQuiz.getId());
        assertTrue(foundQuizItems.size() > 0, "퀴즈 아이템이 존재해야 합니다.");
        assertTrue(foundQuizItems.get(0).getQuiz().getId().equals(testQuiz.getId()), "저장된 퀴즈 아이템의 퀴즈 ID가 일치해야 합니다.");
    }

    @Test
    @DisplayName("퀴즈 아이템 존재 여부 확인 테스트")
    void existsByQuizIdAndQuestionTypeTest() {
        // Save the quiz item
        quizItemRepository.save(testQuizItem);
        entityManager.flush();
        entityManager.clear();

        // Check if the quiz item exists by quiz ID and question type
        boolean exists = quizItemRepository.existsByQuizIdAndQuestionTypeAndDeletedAtIsNull(
                testQuiz.getId(), testQuizItem.getQuestionType());
        
        assertTrue(exists, "퀴즈 아이템이 존재해야 합니다.");
    }

    @Test
    @DisplayName("퀴즈 아이템 업데이트 테스트")
    void updateQuizItemTest() {
        // Save the quiz item
        quizItemRepository.save(testQuizItem);
        entityManager.flush();
        entityManager.clear();

        // Update the quiz item
        testQuizItem.setQuestion("오렌지는 과일입니다.");
        QuizItem updatedQuizItem = quizItemRepository.updateQuizItem(testQuizItem);
        assertThat(updatedQuizItem).isNotNull();
        assertThat(updatedQuizItem.getQuestion()).isEqualTo("오렌지는 과일입니다.");
    
        // Find the updated quiz item by ID
        QuizItem foundQuizItem = entityManager.find(QuizItem.class, updatedQuizItem.getId());
        assertThat(foundQuizItem).isNotNull();
        assertThat(foundQuizItem.getQuestion()).isEqualTo("오렌지는 과일입니다.");
    }
}
