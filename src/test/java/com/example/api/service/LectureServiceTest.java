package com.example.api.service;

import com.example.api.entity.Course;
import com.example.api.entity.Lecture;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.lecture.CreateLectureInput;
import com.example.api.service.dto.lecture.LectureListOutput;
import com.example.api.service.dto.lecture.LectureOutput;
import com.example.api.service.dto.lecture.UpdateLectureInput;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class LectureServiceTest {
    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LectureServiceImpl lectureService;

    private UUID userId;
    private UUID courseId;
    private UUID semesterId;
    private UUID lectureId;

    private User testUser;
    private Course testCourse;
    private Semester testSemester;
    private Lecture testLecture;
    private LectureOutput testLectureOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        semesterId = UUID.randomUUID();
        lectureId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);

        testSemester = new Semester();
        testSemester.setId(semesterId);
        testSemester.setUser(testUser);
        testSemester.setName("2025 Spring 학기");

        testCourse = new Course();
        testCourse.setId(courseId);
        testCourse.setUser(testUser);
        testCourse.setSemester(testSemester);
        testCourse.setName("운영체제");
        testCourse.setTargetGrade(4.0f);
        testCourse.setEarnedGrade(0.0f);
        testCourse.setCompletedCredits(3);
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse.setUpdatedAt(LocalDateTime.now());

        testLecture = new Lecture();
        testLecture.setId(lectureId);
        testLecture.setUser(testUser);
        testLecture.setCourse(testCourse);
        testLecture.setTitle("Introduction to Operating Systems");
        testLecture.setMaterialPath("path/to/material");
        testLecture.setMaterialType("pdf");
        testLecture.setDisplayOrderLex("1");
        testLecture.setNote(Map.of("key", "value"));
        testLecture.setSummary(Map.of("summaryKey", "summaryValue"));
        testLecture.setSummaryStatus(SummaryStatus.not_started);
        testLecture.setCreatedAt(LocalDateTime.now());
        testLecture.setUpdatedAt(LocalDateTime.now());

        testLectureOutput = LectureOutput.fromEntity(testLecture);
    }

    @Test
    @DisplayName("ID로 강의 조회")
    void findLectureById() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));

        // When
        Optional<LectureOutput> output = lectureService.findLectureById(lectureId);

        // Then
        assertTrue(output.isPresent());
        assertEquals(testLectureOutput.getId(), output.get().getId());
        assertEquals(testLectureOutput.getTitle(), output.get().getTitle());
        verify(lectureRepository).findById(lectureId);
    }

    @Test
    @DisplayName("과목 ID로 강의 목록 조회")
    void findLecturesByCourseId() {
        // Given
        when(lectureRepository.findByCourseId(courseId)).thenReturn(Arrays.asList(testLecture));

        // When
        LectureListOutput output = lectureService.findLecturesByCourseId(courseId);

        // Then
        assertNotNull(output);
        assertEquals(1, output.getLectures().size());
        assertEquals(testLectureOutput.getId(), output.getLectures().get(0).getId());
        verify(lectureRepository).findByCourseId(courseId);
    }

    @Test
    @DisplayName("강의 생성")
    void createLecture() {
        // Given
        CreateLectureInput input = new CreateLectureInput();
        input.setUserId(userId);
        input.setCourseId(courseId);
        input.setTitle("Test Title");
        input.setMaterialPath("test/path");
        input.setMaterialType("pdf");
        input.setDisplayOrderLex("2");

        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(courseRepository.getReferenceById(courseId)).thenReturn(testCourse);
        when(lectureRepository.createLecture(any(Lecture.class))).thenReturn(testLecture);

        // When
        LectureOutput output = lectureService.createLecture(input);

        // Then
        assertNotNull(output);
        assertEquals(testLectureOutput.getId(), output.getId());
        assertEquals(testLectureOutput.getTitle(), output.getTitle());
        verify(userRepository).getReferenceById(userId);
        verify(courseRepository).getReferenceById(courseId);
        verify(lectureRepository).createLecture(any(Lecture.class));
    }

    @Test
    @DisplayName("강의 업데이트")
    void updateLecture() {
        // Given
        UpdateLectureInput input = new UpdateLectureInput();
        input.setId(lectureId);
        input.setTitle("Updated Title");
        input.setMaterialPath("updated/path");
        input.setMaterialType("pdf");

        when(lectureRepository.updateLecture(any(Lecture.class))).thenReturn(testLecture);

        // When
        LectureOutput output = lectureService.updateLecture(input);

        // Then
        assertNotNull(output);
        assertEquals(testLectureOutput.getId(), output.getId());
        assertEquals(testLectureOutput.getTitle(), output.getTitle());
        verify(lectureRepository).updateLecture(any(Lecture.class));
    }

    @Test
    @DisplayName("강의 삭제")
    void deleteLecture() {
        // Given
        doNothing().when(lectureRepository).deleteLecture(lectureId);

        // When
        lectureService.deleteLecture(lectureId);

        // Then
        verify(lectureRepository).deleteLecture(lectureId);
    }
}
