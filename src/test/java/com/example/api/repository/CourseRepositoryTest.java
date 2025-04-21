package com.example.api.repository;

import com.example.api.entity.Course;
import com.example.api.entity.School;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.Season;
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
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CourseRepositoryTest {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;

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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("과목 저장 및 ID로 조회 테스트")
    void saveAndFindCourseTest() {
        // Given
        courseRepository.save(testCourse);

        // When
        Optional<Course> found = courseRepository.findById(testCourse.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("운영체제", found.get().getName());
    }

    @Test
    @DisplayName("학기 ID로 과목 목록 조회 테스트")
    void findBySemesterIdTest() {
        // Given
        courseRepository.save(testCourse);

        Course anotherCourse = new Course();
        anotherCourse.setId(UUID.randomUUID());
        anotherCourse.setSemester(testSemester);
        anotherCourse.setUser(testUser);
        anotherCourse.setName("알고리즘");
        courseRepository.save(anotherCourse);

        // When
        List<Course> courses = courseRepository.findBySemesterId(testSemester.getId());

        // Then
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting("name")
                .containsExactlyInAnyOrder("운영체제", "알고리즘");
    }

    @Test
    @DisplayName("사용자 ID로 과목 목록 조회 테스트")
    void findByUserIdTest() {
        // Given
        courseRepository.save(testCourse);

        // When
        List<Course> courses = courseRepository.findByUserId(testUser.getId());

        // Then
        assertThat(courses).hasSize(1);
        assertEquals("운영체제", courses.get(0).getName());
    }

    @Test
    @DisplayName("과목 생성 테스트")
    void createCourseTest() {
        // When
        Course created = courseRepository.createCourse(testCourse);

        // Then
        assertNotNull(created.getId());

        Course fromDb = entityManager.find(Course.class, created.getId());
        assertNotNull(fromDb);
        assertEquals("운영체제", fromDb.getName());
    }

    @Test
    @DisplayName("중복 과목 생성 시 예외 발생 테스트")
    void createDuplicateCourseTest() {
        // Given
        courseRepository.createCourse(testCourse);

        entityManager.flush();
        entityManager.clear();

        // When/Then: 같은 학기, 같은 이름으로 다시 생성 시도하면 예외 발생
        Course duplicateCourse = new Course();
        duplicateCourse.setId(UUID.randomUUID());
        duplicateCourse.setSemester(entityManager.find(Semester.class, testSemester.getId()));
        duplicateCourse.setUser(entityManager.find(User.class, testUser.getId()));
        duplicateCourse.setName("운영체제");

        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            courseRepository.createCourse(duplicateCourse);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("과목 업데이트 테스트")
    void updateCourseTest() {
        // Given
        courseRepository.createCourse(testCourse);

        // When
        testCourse.setName("고급 운영체제");
        Course updated = courseRepository.updateCourse(testCourse);

        // Then
        assertEquals("고급 운영체제", updated.getName());

        Course fromDb = entityManager.find(Course.class, testCourse.getId());
        assertEquals("고급 운영체제", fromDb.getName());
    }

    @Test
    @DisplayName("과목 soft delete 테스트")
    void deleteCourseTest() {
        // Given
        courseRepository.createCourse(testCourse);
        UUID courseId = testCourse.getId();

        // When
        courseRepository.deleteCourse(courseId);

        // Then
        Optional<Course> foundAfterDelete = courseRepository.findById(
                testSemester.getId());
        assertTrue(foundAfterDelete.isEmpty(), "Soft delete 된 과목는 조회되지 않아야 합니다");

        // 하지만 EntityManager로 직접 조회하면 deletedAt 필드가 설정되어 있어야 함
        Course deletedCourse = entityManager.find(Course.class, courseId);
        assertNotNull(deletedCourse, "엔티티는 여전히 데이터베이스에 존재해야 합니다");
        assertNotNull(deletedCourse.getDeletedAt(), "deletedAt 필드가 설정되어 있어야 합니다");
    }

    @Test
    @DisplayName("존재하지 않는 과목 삭제 시 오류 없이 처리되는지 테스트")
    void deleteNonExistentCourseTest() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then: 존재하지 않는 과목 삭제 시도해도 예외 발생하지 않음
        assertDoesNotThrow(() -> {
            courseRepository.deleteCourse(nonExistentId);
        });
    }
}