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
public class LikedQuizItemRepositoryTest {
    @Autowired
    private LikedQuizItemRepository likedQuizItemRepository;
    
    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Quiz testQuiz;
    private QuizItem testQuizItem;
    private LikedQuizItem testLikedQuizItem;

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

        testLikedQuizItem = new LikedQuizItem();
        testLikedQuizItem.setId(UUID.randomUUID());
        testLikedQuizItem.setQuiz(testQuiz);
        testLikedQuizItem.setQuizItem(testQuizItem);
        testLikedQuizItem.setUser(testUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("좋아요한 퀴즈 아이템 저장 및 ID로 조회 테스트")
    void saveAndFindLikedQuizItemTest() {
        // Given
        likedQuizItemRepository.save(testLikedQuizItem);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<LikedQuizItem> found = likedQuizItemRepository.findById(testLikedQuizItem.getId());

        // Then
        assertTrue(found.isPresent());
        assertTrue(found.get().getQuiz().getId().equals(testQuiz.getId()));
        assertTrue(found.get().getQuizItem().getId().equals(testQuizItem.getId()));
    }

    @Test
    @DisplayName("퀴즈 아이템 ID로 좋아요한 퀴즈 아이템 조회 테스트")
    void findByQuizItemIdTest() {
        // Given
        likedQuizItemRepository.save(testLikedQuizItem);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<LikedQuizItem> found = likedQuizItemRepository.findByQuizItemId(testQuizItem.getId());

        // Then
        assertTrue(found.isPresent());
        assertTrue(found.get().getQuiz().getId().equals(testQuiz.getId()));
        assertTrue(found.get().getQuizItem().getId().equals(testQuizItem.getId()));
    }

    @Test
    @DisplayName("좋아요한 퀴즈 아이템 생성(createLikedQuizItem) 테스트")
    void createLikedQuizItemTest() {
        // Given
        LikedQuizItem newLikedQuizItem = new LikedQuizItem();
        newLikedQuizItem.setId(UUID.randomUUID());
        newLikedQuizItem.setQuiz(testQuiz);
        newLikedQuizItem.setQuizItem(testQuizItem);
        newLikedQuizItem.setUser(testUser);

        // When
        LikedQuizItem created = likedQuizItemRepository.createLikedQuizItem(newLikedQuizItem);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertTrue(created.getId().equals(newLikedQuizItem.getId()));
    }

    @Test
    @DisplayName("좋아요한 퀴즈 아이템 삭제(deleteLikedQuizItem) 테스트")
    void deleteLikedQuizItemTest() {
        // Given
        likedQuizItemRepository.save(testLikedQuizItem);
        entityManager.flush();
        entityManager.clear();

        // When
        likedQuizItemRepository.deleteLikedQuizItem(testLikedQuizItem.getId());
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<LikedQuizItem> found = likedQuizItemRepository.findById(testLikedQuizItem.getId());
        assertTrue(found.isEmpty());
    }
}
