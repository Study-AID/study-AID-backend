package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.*;
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
public class QuizQuestionReportRepositoryTest {
    @Autowired
    private QuizQuestionReportRepository quizQuestionReportRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindQuizQuestionReportRepositoryTest() {
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

        UUID lectureUuid = UUID.randomUUID();
        Lecture lecture = new Lecture();
        lecture.setId(lectureUuid);
        lecture.setCourse(course);
        lecture.setUser(user);
        lecture.setTitle("Intro.");
        lecture.setMaterialPath("");
        lecture.setMaterialType("pdf");
        lecture.setDisplayOrderLex("");
        lecture.setSummaryStatus(SummaryStatus.not_started);

        entityManager.persist(lecture);

        UUID quizUuid = UUID.randomUUID();
        Quiz quiz = new Quiz();
        quiz.setId(quizUuid);
        quiz.setLecture(lecture);
        quiz.setUser(user);
        quiz.setTitle("Quiz 1");
        quiz.setStatus(Status.not_started);

        entityManager.persist(quiz);

        UUID quizItemUuid = UUID.randomUUID();
        QuizItem quizItem = new QuizItem();
        quizItem.setId(quizItemUuid);
        quizItem.setQuiz(quiz);
        quizItem.setUser(user);
        quizItem.setQuestion("오렌지는");
        quizItem.setQuestionType(QuestionType.short_answer);

        entityManager.persist(quizItem);

        UUID quizQuestionReportUuid = UUID.randomUUID();
        QuizQuestionReport quizQuestionReport = new QuizQuestionReport();
        quizQuestionReport.setId(quizQuestionReportUuid);
        quizQuestionReport.setQuiz(quiz);
        quizQuestionReport.setQuizItem(quizItem);
        quizQuestionReport.setUser(user);
        quizQuestionReport.setReportReason("그냥 싫어요.");

        quizQuestionReportRepository.save(quizQuestionReport);
        Optional<QuizQuestionReport> found = quizQuestionReportRepository.findById(quizQuestionReportUuid);

        assertTrue(found.isPresent());
    }
}
