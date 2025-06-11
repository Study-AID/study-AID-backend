package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.lecture.*;
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
    private ParsedText testParsedText;

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

        // Create test parsed text
        testParsedText = new ParsedText();
        testParsedText.setTotalPages(2);
        testParsedText.setPages(Arrays.asList(
                new ParsedPage(1, "Page 1 content"),
                new ParsedPage(2, "Page 2 content")
        ));
        testLecture.setParsedText(testParsedText);

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

        // Verify parsedText
        assertNotNull(output.get().getParsedText());
        assertEquals(2, output.get().getParsedText().getTotalPages());
        assertEquals(2, output.get().getParsedText().getPages().size());
        assertEquals(1, output.get().getParsedText().getPages().get(0).getPageNumber());
        assertEquals("Page 1 content", output.get().getParsedText().getPages().get(0).getText());
        assertEquals(2, output.get().getParsedText().getPages().get(1).getPageNumber());
        assertEquals("Page 2 content", output.get().getParsedText().getPages().get(1).getText());

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

        // Verify parsedText in the list
        LectureOutput lecture = output.getLectures().get(0);
        assertNotNull(lecture.getParsedText());
        assertEquals(2, lecture.getParsedText().getTotalPages());
        assertEquals(2, lecture.getParsedText().getPages().size());
        assertEquals("Page 1 content", lecture.getParsedText().getPages().get(0).getText());
        assertEquals("Page 2 content", lecture.getParsedText().getPages().get(1).getText());

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

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.updateLecture(any(Lecture.class))).thenReturn(testLecture);

        // When
        LectureOutput output = lectureService.updateLecture(input);

        // Then
        assertNotNull(output);
        assertEquals(testLectureOutput.getId(), output.getId());
        assertEquals("Updated Title", output.getTitle());
        verify(lectureRepository, times(1)).findById(lectureId);
        verify(lectureRepository, times(1)).updateLecture(any(Lecture.class));
    }

    @Test
    @DisplayName("강의 업데이트 - 강의 노트")
    void updateLectureNote() {
        // Given
        UpdateLectureNoteInput input = new UpdateLectureNoteInput();
        input.setId(lectureId);
        input.setNote(Map.of("key", "업데이트된 노트 내용"));

        // updatedLecture is a clone of testLecture to avoid modifying the original
        Lecture updatedLecture = new Lecture();
        updatedLecture.setId(lectureId);
        updatedLecture.setUser(testLecture.getUser());
        updatedLecture.setCourse(testLecture.getCourse());
        updatedLecture.setTitle(testLecture.getTitle());
        updatedLecture.setMaterialPath(testLecture.getMaterialPath());
        updatedLecture.setMaterialType(testLecture.getMaterialType());
        updatedLecture.setDisplayOrderLex(testLecture.getDisplayOrderLex());
        // Set the note to the new value
        updatedLecture.setNote(input.getNote());
        updatedLecture.setParsedText(testLecture.getParsedText());
        updatedLecture.setSummary(testLecture.getSummary());
        updatedLecture.setSummaryStatus(testLecture.getSummaryStatus());
        updatedLecture.setCreatedAt(testLecture.getCreatedAt());
        updatedLecture.setUpdatedAt(LocalDateTime.now());

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.updateLecture(any(Lecture.class))).thenReturn(updatedLecture);

        // When
        LectureOutput output = lectureService.updateLectureNote(input);

        // Then
        assertNotNull(output);
        assertEquals(testLectureOutput.getId(), output.getId());
        assertEquals("업데이트된 노트 내용", output.getNote().get("key"));
        assertEquals(testLectureOutput.getTitle(), output.getTitle());
        assertEquals(testLectureOutput.getMaterialPath(), output.getMaterialPath());
        assertEquals(testLectureOutput.getMaterialType(), output.getMaterialType());
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

