package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ExamQuestionReportRepositoryTest {
    @Autowired
    private ExamQuestionReportRepository examQuestionReportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindExamQuestionReportRepositoryTest() {
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

        UUID examQuestionReportUuid = UUID.randomUUID();
        ExamQuestionReport examQuestionReport = new ExamQuestionReport();
        examQuestionReport.setId(examQuestionReportUuid);
        examQuestionReport.setExam(exam);
        examQuestionReport.setUser(user);

        examQuestionReportRepository.save(examQuestionReport);
        Optional<ExamQuestionReport> found = examQuestionReportRepository.findById(examQuestionReportUuid);

        assertTrue(found.isPresent());
    }
}
