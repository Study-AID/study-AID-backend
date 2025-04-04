package com.example.api.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.api.entity.Course;
import com.example.api.entity.Exam;
import com.example.api.entity.ExamItem;
import com.example.api.entity.ExamResponse;
import com.example.api.entity.School;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExamResponseRepositoryTest {
    @Autowired
    private ExamResponseRepository examResponseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindExamRepositoryTest() {
        UUID schoolUUID = UUID.randomUUID();
        School school = new School();
        school.setId(schoolUUID);
        school.setName("Ajou");
        entityManager.persist(school);
   
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

        UUID examUuid = UUID.randomUUID();
        Exam exam = new Exam();
        exam.setId(examUuid);
        exam.setCourse(course);
        exam.setUser(user);
        exam.setStatus(Status.not_started);

        entityManager.persist(exam);

        UUID examItemUuid = UUID.randomUUID();
        ExamItem examItem = new ExamItem();
        examItem.setId(examItemUuid);
        examItem.setExam(exam);
        examItem.setUser(user);
        examItem.setQuestion("사과는");
        examItem.setQuestionType(QuestionType.short_answer);
    
        entityManager.persist(examItem);
        
        UUID examResponseUuid = UUID.randomUUID();
        ExamResponse examResponse = new ExamResponse();
        examResponse.setId(examResponseUuid);
        examResponse.setExam(exam);
        examResponse.setExamItem(examItem);
        examResponse.setUser(user);

        examResponseRepository.save(examResponse);
        Optional<ExamResponse> found = examResponseRepository.findById(examResponseUuid);

        assertTrue(found.isPresent());
    }

}
