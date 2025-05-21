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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ExamRepositoryTest {
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;
    private Exam testExam;

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
        testExam.setCourse(testCourse);
        testExam.setUser(testUser);
        testExam.setTitle("Midterm Exam");
        testExam.setStatus(Status.generate_in_progress);
        testExam.setReferencedLectures(new UUID[]{testLecture.getId()});
        testExam.setContentsGenerateAt(LocalDateTime.now());

        entityManager.flush();
        entityManager.clear();
    }


    @Test
    @DisplayName("시험 저장 및 ID로 조회 테스트")
    void testSaveAndFindById() {
        // Save the exam
        examRepository.save(testExam);
        entityManager.flush();
        entityManager.clear();

        // Find the exam by ID
        Optional<Exam> foundExam = examRepository.findById(testExam.getId());

        // Assertions
        assertTrue(foundExam.isPresent());
        assertEquals(testExam.getId(), foundExam.get().getId());
        assertEquals(testExam.getTitle(), foundExam.get().getTitle());
    }

    @Test
    @DisplayName("과목 ID로 시험 목록 조회 테스트")
    void testFindByCourseId() {
        // Save the exam
        examRepository.save(testExam);
        entityManager.flush();
        entityManager.clear();

        // Find exams by course ID
        var foundExams = examRepository.findByCourseId(testCourse.getId());

        // Assertions
        assertTrue(foundExams.size() > 0);
        assertEquals(testExam.getTitle(), foundExams.get(0).getTitle());
    }

    @Test
    @DisplayName("시험 생성 테스트")
    void testCreateExam() {
        // Create a new exam
        Exam newExam = new Exam();
        newExam.setId(UUID.randomUUID());
        newExam.setCourse(testCourse);
        newExam.setUser(testUser);
        newExam.setTitle("Final Exam");
        newExam.setStatus(Status.generate_in_progress);
        newExam.setContentsGenerateAt(LocalDateTime.now());

        // Save the exam
        Exam createdExam = examRepository.createExam(newExam);
        entityManager.flush();
        entityManager.clear();

        // Find the exam by ID
        Optional<Exam> foundExam = examRepository.findById(createdExam.getId());

        // Assertions
        assertTrue(foundExam.isPresent());
        assertEquals(createdExam.getId(), foundExam.get().getId());
        assertEquals(createdExam.getTitle(), foundExam.get().getTitle());   

    }
}
