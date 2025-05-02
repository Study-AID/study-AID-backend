package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.Season;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class LectureRepositoryTest {    
    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private Lecture testLecture;

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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("강의 저장 및 ID로 조회 테스트")
    void saveAndFindLectureTest() {
        // Given
        lectureRepository.save(testLecture);

        // When
        Optional<Lecture> found = lectureRepository.findById(testLecture.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Intro.", found.get().getTitle());
    }

    @Test
    @DisplayName("과목 ID로 강의 목록 조회 테스트")
    void findByCourseIdTest() {
        // Given
        lectureRepository.save(testLecture);

        Lecture anotherLecture = new Lecture();
        anotherLecture.setId(UUID.randomUUID());
        anotherLecture.setCourse(testCourse);
        anotherLecture.setUser(testUser);
        anotherLecture.setTitle("운영체제 1장");
        anotherLecture.setMaterialPath("");
        anotherLecture.setMaterialType("pdf");
        anotherLecture.setDisplayOrderLex("");
        anotherLecture.setSummaryStatus(SummaryStatus.not_started);
        lectureRepository.save(anotherLecture);

        // When
        List<Lecture> lectures = lectureRepository.findByCourseId(testCourse.getId());

        // Then
        assertThat(lectures).hasSize(2);
    }

    @Test
    @DisplayName("사용자 ID로 강의 목록 조회 테스트")
    void findByUserIdTest() {
        // Given
        lectureRepository.save(testLecture);

        // When
        List<Lecture> lectures = lectureRepository.findByUserId(testUser.getId());

        // Then
        assertThat(lectures).hasSize(1);
        assertEquals("Intro.", lectures.get(0).getTitle());
    }

    @Test
    @DisplayName("강의 생성 테스트")
    void createLectureTest() {
        // When
        Lecture createdLecture = lectureRepository.createLecture(testLecture);

        // Then
        assertThat(createdLecture).isNotNull();
        assertThat(createdLecture.getId()).isNotNull();

        Lecture fromDb = entityManager.find(Lecture.class, createdLecture.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getTitle()).isEqualTo("Intro.");
    }

    @Test
    @DisplayName("중복 강의 생성 시 예외 발생 테스트")
    void createDuplicateLectureTest() {
        // Given
        lectureRepository.save(testLecture);

        entityManager.flush();
        entityManager.clear();

        Lecture duplicateLecture = new Lecture();
        duplicateLecture.setId(testLecture.getId());
        duplicateLecture.setCourse(entityManager.find(Course.class, testCourse.getId()));
        duplicateLecture.setUser(entityManager.find(User.class, testUser.getId()));
        duplicateLecture.setTitle("Intro.");
        duplicateLecture.setMaterialPath("");
        duplicateLecture.setMaterialType("pdf");
        duplicateLecture.setDisplayOrderLex("");
        duplicateLecture.setSummaryStatus(SummaryStatus.not_started);

        // When & Then
        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            lectureRepository.createLecture(duplicateLecture);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("강의 업데이트 테스트")
    void updateLectureTest() {
        // Given
        lectureRepository.createLecture(testLecture);

        // When
        testLecture.setTitle("Updated Title");
        Lecture updatedLecture = lectureRepository.updateLecture(testLecture);

        // Then
        assertThat(updatedLecture).isNotNull();
        assertThat(updatedLecture.getTitle()).isEqualTo("Updated Title");

        Lecture fromDb = entityManager.find(Lecture.class, updatedLecture.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("강의 삭제(soft delete) 테스트")
    void deleteLectureTest() {
        // Given
        lectureRepository.createLecture(testLecture);

        // When
        lectureRepository.deleteLecture(testLecture.getId());

        // Then
        Lecture deletedLecture = entityManager.find(Lecture.class, testLecture.getId());
        assertThat(deletedLecture).isNotNull();
        assertThat(deletedLecture.getDeletedAt()).isNotNull();
    }

}
