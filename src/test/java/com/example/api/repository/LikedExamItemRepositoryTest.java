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
public class LikedExamItemRepositoryTest {
    @Autowired
    private LikedExamItemRepository likedExamItemRepository;
    
    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Exam testExam;
    private ExamItem testExamItem;
    private LikedExamItem testLikedExamItem;

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

        testExam = new Exam();
        testExam.setId(UUID.randomUUID());
        testExam.setUser(testUser);
        testExam.setCourse(testCourse);
        testExam.setTitle("Midterm Exam");
        testExam.setStatus(Status.not_started);
        entityManager.persist(testExam);

        testExamItem = new ExamItem();
        testExamItem.setId(UUID.randomUUID());
        testExamItem.setExam(testExam);
        testExamItem.setUser(testUser);
        testExamItem.setQuestion("what is the capital of France?");
        testExamItem.setQuestionType(QuestionType.short_answer);
        entityManager.persist(testExamItem);

        testLikedExamItem = new LikedExamItem();
        testLikedExamItem.setId(UUID.randomUUID());
        testLikedExamItem.setUser(testUser);
        testLikedExamItem.setExam(testExam);
        testLikedExamItem.setExamItem(testExamItem);
        testLikedExamItem.setCreatedAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("LikedExamItem 저장 및 조회 테스트")
    void saveAndFindLikedExamItemTest() {
        // LikedExamItem 저장
        likedExamItemRepository.save(testLikedExamItem);
        entityManager.flush();
        entityManager.clear();

        // ID로 LikedExamItem 조회
        Optional<LikedExamItem> foundLikedExamItem = likedExamItemRepository.findById(testLikedExamItem.getId());
        
        // 조회된 LikedExamItem이 존재하는지 확인
        assertTrue(foundLikedExamItem.isPresent());
    }

    @Test
    @DisplayName("시험 아이템 ID와 사용자 ID로 좋아요한 시험 아이템 조회 테스트")
    void findByExamItemIdAndUserIdTest() {
        // LikedExamItem 저장
        likedExamItemRepository.save(testLikedExamItem);
        entityManager.flush();
        entityManager.clear();

        // 시험 아이템 ID와 사용자 ID로 LikedExamItem 조회
        Optional<LikedExamItem> foundLikedExamItem = likedExamItemRepository.findByExamItemIdAndUserId(
                testExamItem.getId(), testUser.getId());

        // 조회된 LikedExamItem이 존재하는지 확인
        assertTrue(foundLikedExamItem.isPresent());
    }

    @Test
    @DisplayName("좋아요한 시험 아이템 생성(createLikedQuizItem) 테스트")
    void createLikedExamItemTest() {
        // Given
        LikedExamItem newLikedExamItem = new LikedExamItem();
        newLikedExamItem.setId(UUID.randomUUID());
        newLikedExamItem.setUser(testUser);
        newLikedExamItem.setExam(testExam);
        newLikedExamItem.setExamItem(testExamItem);
        newLikedExamItem.setCreatedAt(LocalDateTime.now());

        // When
        LikedExamItem created = likedExamItemRepository.createLikedExamItem(newLikedExamItem);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertTrue(created.getId().equals(newLikedExamItem.getId()));
    }

    @Test
    @DisplayName("좋아요한 시험 아이템 삭제(createLikedExamItem) 테스트")
    void deleteLikedExamItemTest() {
        // Given
        likedExamItemRepository.save(testLikedExamItem);
        entityManager.flush();
        entityManager.clear();

        // When
        likedExamItemRepository.deleteLikedExamItem(testLikedExamItem.getId());
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<LikedExamItem> found = likedExamItemRepository.findById(testLikedExamItem.getId());
        assertTrue(found.isEmpty());
    }
}
