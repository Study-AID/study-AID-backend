package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ExamItemRepositoryTest {
    @Autowired
    private ExamItemRepository examItemRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Exam testExam;
    private ExamItem testExamItem;
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

        testExam = new Exam();
        testExam.setId(UUID.randomUUID());
        testExam.setCourse(testCourse);
        testExam.setUser(testUser);
        testExam.setStatus(Status.not_started);

        entityManager.persist(testExam);

        testExamItem = new ExamItem();
        testExamItem.setId(UUID.randomUUID());
        testExamItem.setExam(testExam);
        testExamItem.setUser(testUser);
        testExamItem.setQuestion("사과는");
        testExamItem.setQuestionType(QuestionType.short_answer);
        testExamItem.setIsLiked(false);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("시험 아이템 저장 및 ID로 조회 테스트")
    void saveAndFindByIdTest() {
        // 시험 아이템 저장
        ExamItem savedExamItem = examItemRepository.save(testExamItem);
        entityManager.flush();
        entityManager.clear();

        // ID로 시험 아이템 조회
        Optional<ExamItem> foundExamItem = examItemRepository.findById(savedExamItem.getId());

        // 조회된 시험 아이템이 존재하고, 내용이 일치하는지 확인
        assertTrue(foundExamItem.isPresent());
        assertEquals(savedExamItem.getId(), foundExamItem.get().getId());
        assertEquals(testExamItem.getQuestion(), foundExamItem.get().getQuestion());
    }

    @Test
    @DisplayName("시험 아이템 저장 및 시험 ID로 조회 테스트")
    void saveAndFindByExamIdTest() {
        // 시험 아이템 저장
        examItemRepository.save(testExamItem);
        entityManager.flush();
        entityManager.clear();

        // 시험 ID로 시험 아이템 조회
        List<ExamItem> foundExamItems = examItemRepository.findByExamId(testExam.getId());

        // 조회된 시험 아이템이 존재하고, 내용이 일치하는지 확인
        assertTrue(foundExamItems.size() > 0, "시험 아이템이 존재해야 합니다.");
        assertTrue(foundExamItems.get(0).getId().equals(testExamItem.getId()), "저장된 시험 아이템의 ID가 일치해야 합니다.");
    }

    @Test
    @DisplayName("시험 아이템 존재 여부 확인 테스트")
    void existsByExamIdAndQuestionTypeTest() {
        // 시험 아이템 저장
        examItemRepository.save(testExamItem);
        entityManager.flush();
        entityManager.clear();

        // 시험 ID와 질문 유형으로 존재 여부 확인
        boolean exists = examItemRepository.existsByExamIdAndQuestionTypeAndDeletedAtIsNull(
                testExam.getId(), testExamItem.getQuestionType());

        assertTrue(exists, "시험 아이템이 존재해야 합니다.");
    }

    @Test
    @DisplayName("시험 아이템 업데이트 테스트")
    void updateExamItemTest() {
        // 시험 아이템 저장
        examItemRepository.save(testExamItem);
        entityManager.flush();
        entityManager.clear();

        // 시험 아이템 업데이트
        testExamItem.setQuestion("업데이트된 질문");
        ExamItem updatedExamItem = examItemRepository.updateExamItem(testExamItem);
        assertThat(updatedExamItem).isNotNull();
        assertThat(updatedExamItem.getQuestion()).isEqualTo("업데이트된 질문");

        // 업데이트된 시험 아이템이 데이터베이스에 반영되었는지 확인
        ExamItem foundExamItem = entityManager.find(ExamItem.class, updatedExamItem.getId());
        assertThat(foundExamItem).isNotNull();
        assertThat(foundExamItem.getQuestion()).isEqualTo("업데이트된 질문");
    }
}
