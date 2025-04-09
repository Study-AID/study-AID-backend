package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.api.entity.School;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.Season;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)    // 스프링이 DataSource를 인메모리DB로 교체하는 것을 막음.
public class SemesterRepositoryTest {

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindSemesterTest() {
        UUID schoolUuid = UUID.randomUUID();
        School school = new School();
        school.setId(schoolUuid);
        school.setName("Ajou");
        entityManager.persist(school);
   
        // Given: 외래키로 연결된 User 엔티티를 먼저 저장
        UUID userUuid = UUID.randomUUID();
        User user = new User();
        user.setId(userUuid);
        user.setSchool(school);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAuthType(AuthType.email);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user); // user 엔티티 저장

        // Semester 엔티티 생성
        UUID semesterUuid = UUID.randomUUID();
        Semester semester = new Semester();
        semester.setId(semesterUuid);
        semester.setUser(user);
        semester.setName("2025 봄학기");
        semester.setYear(2025);
        semester.setSeason(Season.spring);

        semesterRepository.save(semester);

        Optional<Semester> found = semesterRepository.findById(semesterUuid);
        assertTrue(found.isPresent());
    }
}
