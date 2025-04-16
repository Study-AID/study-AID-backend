package com.example.api.repository;

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
public class SemesterRepositoryTest {
    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Semester testSemester;

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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("학기 저장 및 ID로 조회 테스트")
    void saveAndFindSemesterTest() {
        // Given
        semesterRepository.save(testSemester);

        // When
        Optional<Semester> found = semesterRepository.findById(testSemester.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("2025 봄학기", found.get().getName());
        assertEquals(2025, found.get().getYear());
        assertEquals(Season.spring, found.get().getSeason());
    }

    @Test
    @DisplayName("사용자 ID로 학기 목록 조회 테스트")
    void findByUserIdTest() {
        // Given
        semesterRepository.save(testSemester);

        Semester fallSemester = new Semester();
        fallSemester.setId(UUID.randomUUID());
        fallSemester.setUser(testUser);
        fallSemester.setName("2025 가을학기");
        fallSemester.setYear(2025);
        fallSemester.setSeason(Season.fall);
        semesterRepository.save(fallSemester);

        // When
        List<Semester> semesters = semesterRepository.findByUserId(testUser.getId());

        // Then
        assertThat(semesters).hasSize(2);
        assertThat(semesters).extracting("name")
                .containsExactlyInAnyOrder("2025 봄학기", "2025 가을학기");
    }

    @Test
    @DisplayName("사용자 ID, 연도, 학기로 특정 학기 조회 테스트")
    void findByUserIdAndYearAndSeasonTest() {
        // Given
        semesterRepository.save(testSemester);

        // When
        Optional<Semester> found = semesterRepository.findByUserIdAndYearAndSeason(
                testUser.getId(), 2025, Season.spring);

        // Then
        if (found.isEmpty()) {
            fail("학기를 찾을 수 없습니다");
        } else {
            assertEquals("2025 봄학기", found.get().getName());
            assertEquals(2025, found.get().getYear());
            assertEquals(Season.spring, found.get().getSeason());
        }
    }

    @Test
    @DisplayName("존재하지 않는 학기 조회 시 null 반환 테스트")
    void findNonExistentSemesterTest() {
        // Given

        // When
        Optional<Semester> found = semesterRepository.findByUserIdAndYearAndSeason(
                testUser.getId(), 2024, Season.winter);

        // Then
        assertFalse(found.isPresent(), "존재하지 않는 학기는 조회되지 않아야 합니다");
    }

    @Test
    @DisplayName("학기 생성 테스트")
    void createSemesterTest() {
        // When
        Semester created = semesterRepository.createSemester(testSemester);

        // Then
        assertNotNull(created.getId());

        Semester fromDb = entityManager.find(Semester.class, created.getId());
        assertNotNull(fromDb);
        assertEquals("2025 봄학기", fromDb.getName());
    }

    @Test
    @DisplayName("중복 학기 생성 시 예외 발생 테스트")
    void createDuplicateSemesterTest() {
        // Given
        semesterRepository.createSemester(testSemester);

        entityManager.flush();
        entityManager.clear();

        // 분리된 User 엔티티를 다시 로드
        User refreshedUser = entityManager.find(User.class, testUser.getId());

        // When/Then: 같은 사용자, 연도, 학기로 다시 생성 시도하면 예외 발생
        Semester duplicateSemester = new Semester();
        duplicateSemester.setId(UUID.randomUUID());
        duplicateSemester.setUser(refreshedUser);
        duplicateSemester.setName("2025 봄학기 (중복)");
        duplicateSemester.setYear(2025);
        duplicateSemester.setSeason(Season.spring);

        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            semesterRepository.createSemester(duplicateSemester);
        });

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("학기 업데이트 테스트")
    void updateSemesterTest() {
        // Given
        semesterRepository.createSemester(testSemester);

        // When
        testSemester.setName("2025 수정된 봄학기");
        testSemester.setYear(2026);

        Semester updated = semesterRepository.updateSemester(testSemester);

        // Then
        assertEquals("2025 수정된 봄학기", updated.getName());
        assertEquals(2026, updated.getYear());

        Semester fromDb = entityManager.find(Semester.class, testSemester.getId());
        assertEquals("2025 수정된 봄학기", fromDb.getName());
        assertEquals(2026, fromDb.getYear());
    }

    @Test
    @DisplayName("학기 soft delete 테스트")
    void deleteSemesterTest() {
        // Given
        semesterRepository.createSemester(testSemester);
        UUID semesterId = testSemester.getId();

        // When
        semesterRepository.deleteSemester(semesterId);

        // Then
        Optional<Semester> foundAfterDelete = semesterRepository.findByUserIdAndYearAndSeason(
                testUser.getId(), 2025, Season.spring);
        assertTrue(foundAfterDelete.isEmpty(), "Soft delete 된 학기는 조회되지 않아야 합니다");

        // 하지만 EntityManager로 직접 조회하면 deletedAt 필드가 설정되어 있어야 함
        Semester deletedSemester = entityManager.find(Semester.class, semesterId);
        assertNotNull(deletedSemester, "엔티티는 여전히 데이터베이스에 존재해야 합니다");
        assertNotNull(deletedSemester.getDeletedAt(), "deletedAt 필드가 설정되어 있어야 합니다");
    }

    @Test
    @DisplayName("존재하지 않는 학기 삭제 시 오류 없이 처리되는지 테스트")
    void deleteNonExistentSemesterTest() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then: 존재하지 않는 학기 삭제 시도해도 예외 발생하지 않음
        assertDoesNotThrow(() -> {
            semesterRepository.deleteSemester(nonExistentId);
        });
    }
}