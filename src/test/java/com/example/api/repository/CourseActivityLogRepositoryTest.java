package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.api.entity.Course;
import com.example.api.entity.CourseActivityLog;
import com.example.api.entity.CourseAssessment;
import com.example.api.entity.School;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.Season;

import jakarta.persistence.EntityManager;
import net.minidev.json.JSONObject;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CourseActivityLogRepositoryTest {
    @Autowired
    private CourseActivityLogRepository courseActivityLogRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindCourseActivityLogTest() {
        UUID schoolUUID = UUID.randomUUID();
        School school = new School();
        school.setId(schoolUUID);
        school.setName("Ajou");
        entityManager.persist(school);
   
        // Given: 외래키로 연결된 User 엔티티를 먼저 저장
        UUID userUuid = UUID.randomUUID();
        User user = new User();
        user.setId(userUuid);
        //user.setSchool(school);
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

        entityManager.persist(semester);

        UUID courseUuid = UUID.randomUUID();
        Course course = new Course();
        course.setId(courseUuid);
        course.setSemester(semester);
        course.setUser(user);
        course.setName("운영체제");

        entityManager.persist(course);

        UUID courseActivityLogUuid = UUID.randomUUID();
        CourseActivityLog courseActivityLog = new CourseActivityLog();
        courseActivityLog.setId(courseActivityLogUuid);
        courseActivityLog.setCourse(course);
        courseActivityLog.setUser(user);
        courseActivityLog.setActivityType("activity");
        courseActivityLog.setContentsType("contents");
        JSONObject jsonObject = new JSONObject();
        courseActivityLog.setActivityDetails(jsonObject.toJSONString());

        courseActivityLogRepository.save(courseActivityLog);
        Optional<CourseActivityLog> found = courseActivityLogRepository.findById(courseActivityLogUuid);

        assertTrue(found.isPresent());
        assertEquals("activity", found.get().getActivityType());
    }
}
