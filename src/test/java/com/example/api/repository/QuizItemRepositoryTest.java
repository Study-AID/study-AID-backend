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
import com.example.api.entity.Lecture;
import com.example.api.entity.Quiz;
import com.example.api.entity.QuizItem;
import com.example.api.entity.School;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.Status;
import com.example.api.entity.enums.SummaryStatus;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class QuizItemRepositoryTest {
    @Autowired
    private QuizItemRepository quizItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindQuizItemRepositoryTest() {
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
    
        quizItemRepository.save(quizItem);
        Optional<QuizItem> found = quizItemRepository.findById(quizItemUuid);

        assertTrue(found.isPresent());
    }
}
