package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ExamResultRepositoryTest {
    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Exam testExam;
    private ExamResult testExamResult;
    
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

        testExamResult = new ExamResult();
        testExamResult.setId(UUID.randomUUID());
        testExamResult.setExam(testExam);
        testExamResult.setUser(testUser);
        testExamResult.setScore(10.0f);
        testExamResult.setMaxScore(10.0f);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("시험 결과 저장 및 조회 테스트")
    void testCreateAndFindExamResult() {
        examResultRepository.save(testExamResult);
        entityManager.flush();
        entityManager.clear();

        Optional<ExamResult> foundExamResult = examResultRepository.findById(testExamResult.getId());
        assertTrue(foundExamResult.isPresent(), "시험 결과가 존재해야 합니다.");
        assertEquals(testExamResult.getId(), foundExamResult.get().getId(), "저장된 시험 결과의 ID가 일치해야 합니다.");
        assertEquals(testExamResult.getScore(), foundExamResult.get().getScore(), "저장된 시험 결과의 점수가 일치해야 합니다.");
        assertEquals(testExamResult.getMaxScore(), foundExamResult.get().getMaxScore(), "저장된 시험 결과의 최대 점수가 일치해야 합니다.");
    }

    @Test
    @DisplayName("과목 ID로 시험 결과 조회 테스트")
    void testFindByCourseId() {
        examResultRepository.save(testExamResult);
        entityManager.flush();
        entityManager.clear();

        var foundExamResult = examResultRepository.findByCourseId(testCourse.getId());
        assertTrue(foundExamResult.size() > 0, "과목 ID로 조회된 시험 결과가 있어야 합니다.");
        assertEquals(testExamResult.getId(), foundExamResult.get(0).getId(), "조회된 시험 결과의 ID가 일치해야 합니다.");
    }

    @Test
    @DisplayName("시험 결과 생성 테스트")
    void testCreateExamResult() {
        ExamResult createdExamResult = examResultRepository.createExamResult(testExamResult);

        entityManager.flush();
        entityManager.clear();

        Optional<ExamResult> foundExamResult = examResultRepository.findById(createdExamResult.getId());
        assertTrue(foundExamResult.isPresent(), "시험 결과가 생성되어야 합니다.");
        assertEquals(testExamResult.getId(), foundExamResult.get().getId(), "생성된 시험 결과의 ID가 일치해야 합니다.");
        assertEquals(testExamResult.getScore(), foundExamResult.get().getScore(), "생성된 시험 결과의 점수가 일치해야 합니다.");
        assertEquals(testExamResult.getMaxScore(), foundExamResult.get().getMaxScore(), "생성된 시험 결과의 최대 점수가 일치해야 합니다.");
    }
}
