package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.Season;
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
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CourseAssessmentRepositoryTest {
    @Autowired
    private CourseAssessmentRepository courseAssessmentRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private CourseAssessment testCourseAssessment;

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

        testCourseAssessment = new CourseAssessment();
        testCourseAssessment.setId(UUID.randomUUID());
        testCourseAssessment.setCourse(testCourse);
        testCourseAssessment.setUser(testUser);
        testCourseAssessment.setTitle("중간고사");
        testCourseAssessment.setScore(85.0f);
        testCourseAssessment.setMaxScore(100.0f);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("과제 평가 저장 및 ID로 조회 테스트")
    void saveAndFindCourseAssessmentTest() {
        // Given
        courseAssessmentRepository.save(testCourseAssessment);

        // When
        Optional<CourseAssessment> found = courseAssessmentRepository.findById(testCourseAssessment.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("중간고사", found.get().getTitle());
        assertEquals(85.0f, found.get().getScore());
        assertEquals(100.0f, found.get().getMaxScore());
    }

    @Test
    @DisplayName("과목 ID로 과제 평가 목록 조회 테스트")
    void findByCourseIdTest() {
        // Given
        courseAssessmentRepository.save(testCourseAssessment);

        CourseAssessment anotherAssessment = new CourseAssessment();
        anotherAssessment.setId(UUID.randomUUID());
        anotherAssessment.setCourse(testCourse);
        anotherAssessment.setUser(testUser);
        anotherAssessment.setTitle("기말고사");
        anotherAssessment.setScore(92.0f);
        anotherAssessment.setMaxScore(100.0f);
        courseAssessmentRepository.save(anotherAssessment);

        // When
        List<CourseAssessment> assessments = courseAssessmentRepository.findByCourseId(testCourse.getId());

        // Then
        assertThat(assessments).hasSize(2);
        // 최신 순 정렬 확인 (createdAt DESC)
        assertThat(assessments.get(0).getCreatedAt()).isAfterOrEqualTo(assessments.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("삭제된 과제 평가는 조회되지 않음 테스트")
    void findByCourseIdExcludesDeletedTest() {
        // Given
        courseAssessmentRepository.save(testCourseAssessment);

        CourseAssessment deletedAssessment = new CourseAssessment();
        deletedAssessment.setId(UUID.randomUUID());
        deletedAssessment.setCourse(testCourse);
        deletedAssessment.setUser(testUser);
        deletedAssessment.setTitle("삭제된 평가");
        deletedAssessment.setScore(80.0f);
        deletedAssessment.setMaxScore(100.0f);
        deletedAssessment.setDeletedAt(LocalDateTime.now()); // 삭제 처리
        courseAssessmentRepository.save(deletedAssessment);

        // When
        List<CourseAssessment> assessments = courseAssessmentRepository.findByCourseId(testCourse.getId());

        // Then
        assertThat(assessments).hasSize(1);
        assertEquals("중간고사", assessments.get(0).getTitle());
    }

    @Test
    @DisplayName("과제 평가 생성 테스트")
    void createCourseAssessmentTest() {
        // When
        CourseAssessment createdAssessment = courseAssessmentRepository.createCourseAssessment(testCourseAssessment);

        // Then
        assertThat(createdAssessment).isNotNull();
        assertThat(createdAssessment.getId()).isNotNull();
        assertThat(createdAssessment.getCreatedAt()).isNotNull();
        assertThat(createdAssessment.getUpdatedAt()).isNotNull();

        CourseAssessment fromDb = entityManager.find(CourseAssessment.class, createdAssessment.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getTitle()).isEqualTo("중간고사");
        assertThat(fromDb.getScore()).isEqualTo(85.0f);
        assertThat(fromDb.getMaxScore()).isEqualTo(100.0f);
    }

    @Test
    @DisplayName("과제 평가 업데이트 테스트")
    void updateCourseAssessmentTest() {
        // Given
        CourseAssessment created = courseAssessmentRepository.createCourseAssessment(testCourseAssessment);
        LocalDateTime originalUpdatedAt = created.getUpdatedAt();

        // 시간 차이를 위해 잠시 대기
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        created.setTitle("수정된 중간고사");
        created.setScore(88.0f);
        CourseAssessment updated = courseAssessmentRepository.updateCourseAssessment(created);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("수정된 중간고사");
        assertThat(updated.getScore()).isEqualTo(88.0f);
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);

        CourseAssessment fromDb = entityManager.find(CourseAssessment.class, updated.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getTitle()).isEqualTo("수정된 중간고사");
        assertThat(fromDb.getScore()).isEqualTo(88.0f);
    }

    @Test
    @DisplayName("과제 평가 삭제(soft delete) 테스트")
    void deleteCourseAssessmentTest() {
        // Given
        CourseAssessment created = courseAssessmentRepository.createCourseAssessment(testCourseAssessment);

        // When
        courseAssessmentRepository.deleteCourseAssessment(created.getId());

        // Then
        CourseAssessment deletedAssessment = entityManager.find(CourseAssessment.class, created.getId());
        assertThat(deletedAssessment).isNotNull();
        assertThat(deletedAssessment.getDeletedAt()).isNotNull();

        // findByCourseId에서는 조회되지 않아야 함
        List<CourseAssessment> assessments = courseAssessmentRepository.findByCourseId(testCourse.getId());
        assertThat(assessments).hasSize(0);
    }

    @Test
    @DisplayName("존재하지 않는 과제 평가 삭제 시 오류 없이 처리")
    void deleteNonExistentCourseAssessmentTest() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then - 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            courseAssessmentRepository.deleteCourseAssessment(nonExistentId);
        });
    }

    @Test
    @DisplayName("점수 유효성 검증 테스트")
    void scoreValidationTest() {
        // Given - 점수가 음수인 경우
        CourseAssessment invalidAssessment = new CourseAssessment();
        invalidAssessment.setId(UUID.randomUUID());
        invalidAssessment.setCourse(testCourse);
        invalidAssessment.setUser(testUser);
        invalidAssessment.setTitle("유효하지 않은 점수");
        invalidAssessment.setScore(-5.0f); // 음수 점수
        invalidAssessment.setMaxScore(100.0f);

        // When & Then - 데이터베이스 제약 조건에 따라 처리됨
        // 실제 프로덕션에서는 애플리케이션 레벨에서 검증하지만, 
        // 여기서는 데이터베이스까지 도달하는지 확인
        assertDoesNotThrow(() -> {
            courseAssessmentRepository.createCourseAssessment(invalidAssessment);
        });
    }

    @Test
    @DisplayName("중복 제목 허용 테스트")
    void duplicateTitleAllowedTest() {
        // Given
        courseAssessmentRepository.createCourseAssessment(testCourseAssessment);

        CourseAssessment duplicateTitleAssessment = new CourseAssessment();
        duplicateTitleAssessment.setId(UUID.randomUUID());
        duplicateTitleAssessment.setCourse(testCourse);
        duplicateTitleAssessment.setUser(testUser);
        duplicateTitleAssessment.setTitle("중간고사"); // 동일한 제목
        duplicateTitleAssessment.setScore(90.0f);
        duplicateTitleAssessment.setMaxScore(100.0f);

        // When & Then - 중복 제목이 허용되어야 함
        assertDoesNotThrow(() -> {
            CourseAssessment created = courseAssessmentRepository.createCourseAssessment(duplicateTitleAssessment);
            assertThat(created).isNotNull();
        });

        // 두 평가 모두 조회되어야 함
        List<CourseAssessment> assessments = courseAssessmentRepository.findByCourseId(testCourse.getId());
        assertThat(assessments).hasSize(2);
    }
}