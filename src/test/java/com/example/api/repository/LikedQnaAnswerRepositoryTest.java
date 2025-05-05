package com.example.api.repository;

import com.example.api.entity.*;
import com.example.api.entity.enums.AuthType;
import com.example.api.entity.enums.Season;
import com.example.api.entity.enums.SummaryStatus;
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
public class LikedQnaAnswerRepositoryTest {
    @Autowired
    private LikedQnaAnswerRepository likedQnaAnswerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndFindLectureRepositoryTest() {
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

        UUID qnaChatUuid = UUID.randomUUID();
        QnaChat qnaChat = new QnaChat();
        qnaChat.setId(qnaChatUuid);
        qnaChat.setLecture(lecture);
        qnaChat.setUser(user);

        entityManager.persist(qnaChat);

        UUID qnaChatMessageUuid = UUID.randomUUID();
        QnaChatMessage qnaChatMessage = new QnaChatMessage();
        qnaChatMessage.setId(qnaChatMessageUuid);
        qnaChatMessage.setQnaChat(qnaChat);
        qnaChatMessage.setUser(user);
        qnaChatMessage.setQuestion("이것은 메시지인가요, 메세지인가요.");

        entityManager.persist(qnaChatMessage);

        UUID likedQnaAnswerUuid = UUID.randomUUID();
        LikedQnaAnswer likedQnaAnswer = new LikedQnaAnswer();
        likedQnaAnswer.setId(likedQnaAnswerUuid);
        likedQnaAnswer.setQnaChat(qnaChat);
        likedQnaAnswer.setUser(user);


        likedQnaAnswerRepository.save(likedQnaAnswer);
        Optional<LikedQnaAnswer> found = likedQnaAnswerRepository.findById(likedQnaAnswerUuid);

        assertTrue(found.isPresent());
    }
}
