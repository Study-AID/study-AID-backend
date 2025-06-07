package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.repository.CourseAssessmentRepository;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.courseAssessment.*;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CourseAssessmentServiceTest {
    @Mock
    private CourseAssessmentRepository courseAssessmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseAssessmentServiceImpl courseAssessmentService;

    private UUID userId;
    private UUID courseId;
    private UUID courseAssessmentId;

    private User testUser;
    private Course testCourse;
    private CourseAssessment testCourseAssessment;
    private CourseAssessmentOutput testCourseAssessmentOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        courseAssessmentId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);

        testCourse = new Course();
        testCourse.setId(courseId);
        testCourse.setUser(testUser);
        testCourse.setName("운영체제");
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse.setUpdatedAt(LocalDateTime.now());

        testCourseAssessment = new CourseAssessment();
        testCourseAssessment.setId(courseAssessmentId);
        testCourseAssessment.setUser(testUser);
        testCourseAssessment.setCourse(testCourse);
        testCourseAssessment.setTitle("중간고사");
        testCourseAssessment.setScore(85.0f);
        testCourseAssessment.setMaxScore(100.0f);
        testCourseAssessment.setCreatedAt(LocalDateTime.now());
        testCourseAssessment.setUpdatedAt(LocalDateTime.now());

        testCourseAssessmentOutput = CourseAssessmentOutput.fromEntity(testCourseAssessment);
    }

    @Test
    @DisplayName("ID로 과제 평가 조회")
    void findCourseAssessmentById() {
        // Given
        when(courseAssessmentRepository.findById(courseAssessmentId))
                .thenReturn(Optional.of(testCourseAssessment));

        // When
        Optional<CourseAssessmentOutput> output = courseAssessmentService.findCourseAssessmentById(courseAssessmentId);

        // Then
        assertTrue(output.isPresent());
        assertEquals(testCourseAssessmentOutput.getId(), output.get().getId());
        assertEquals(testCourseAssessmentOutput.getTitle(), output.get().getTitle());
        assertEquals(testCourseAssessmentOutput.getScore(), output.get().getScore());
        assertEquals(testCourseAssessmentOutput.getMaxScore(), output.get().getMaxScore());

        verify(courseAssessmentRepository).findById(courseAssessmentId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 과제 평가 조회")
    void findCourseAssessmentByIdNotFound() {
        // Given
        when(courseAssessmentRepository.findById(courseAssessmentId))
                .thenReturn(Optional.empty());

        // When
        Optional<CourseAssessmentOutput> output = courseAssessmentService.findCourseAssessmentById(courseAssessmentId);

        // Then
        assertFalse(output.isPresent());

        verify(courseAssessmentRepository).findById(courseAssessmentId);
    }

    @Test
    @DisplayName("과목 ID로 과제 평가 목록 조회")
    void findCourseAssessmentsByCourseId() {
        // Given
        CourseAssessment additionalAssessment = new CourseAssessment();
        additionalAssessment.setId(UUID.randomUUID());
        additionalAssessment.setUser(testUser);
        additionalAssessment.setCourse(testCourse);
        additionalAssessment.setTitle("기말고사");
        additionalAssessment.setScore(92.0f);
        additionalAssessment.setMaxScore(100.0f);
        additionalAssessment.setCreatedAt(LocalDateTime.now());
        additionalAssessment.setUpdatedAt(LocalDateTime.now());

        when(courseAssessmentRepository.findByCourseId(courseId))
                .thenReturn(Arrays.asList(testCourseAssessment, additionalAssessment));

        // When
        CourseAssessmentListOutput output = courseAssessmentService.findCourseAssessmentsByCourseId(courseId);

        // Then
        assertNotNull(output);
        assertEquals(2, output.getCourseAssessments().size());
        assertEquals(testCourseAssessmentOutput.getId(), output.getCourseAssessments().get(0).getId());
        assertEquals("중간고사", output.getCourseAssessments().get(0).getTitle());
        assertEquals("기말고사", output.getCourseAssessments().get(1).getTitle());

        verify(courseAssessmentRepository).findByCourseId(courseId);
    }

    @Test
    @DisplayName("과제 평가 생성")
    void createCourseAssessment() {
        // Given
        CreateCourseAssessmentInput input = new CreateCourseAssessmentInput();
        input.setUserId(userId);
        input.setCourseId(courseId);
        input.setTitle("퀴즈");
        input.setScore(95.0f);
        input.setMaxScore(100.0f);

        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(courseRepository.getReferenceById(courseId)).thenReturn(testCourse);
        when(courseAssessmentRepository.createCourseAssessment(any(CourseAssessment.class)))
                .thenReturn(testCourseAssessment);

        // When
        CourseAssessmentOutput output = courseAssessmentService.createCourseAssessment(input);

        // Then
        assertNotNull(output);
        assertEquals(testCourseAssessmentOutput.getId(), output.getId());
        assertEquals(testCourseAssessmentOutput.getTitle(), output.getTitle());
        assertEquals(testCourseAssessmentOutput.getScore(), output.getScore());
        assertEquals(testCourseAssessmentOutput.getMaxScore(), output.getMaxScore());

        verify(userRepository).getReferenceById(userId);
        verify(courseRepository).getReferenceById(courseId);
        verify(courseAssessmentRepository).createCourseAssessment(argThat(courseAssessment ->
                courseAssessment.getTitle().equals("퀴즈") &&
                courseAssessment.getScore().equals(95.0f) &&
                courseAssessment.getMaxScore().equals(100.0f) &&
                courseAssessment.getUser().equals(testUser) &&
                courseAssessment.getCourse().equals(testCourse)
        ));
    }

    @Test
    @DisplayName("과제 평가 업데이트")
    void updateCourseAssessment() {
        // Given
        UpdateCourseAssessmentInput input = new UpdateCourseAssessmentInput();
        input.setId(courseAssessmentId);
        input.setTitle("수정된 중간고사");
        input.setScore(88.0f);
        input.setMaxScore(100.0f);

        CourseAssessment updatedAssessment = new CourseAssessment();
        updatedAssessment.setId(courseAssessmentId);
        updatedAssessment.setUser(testUser);
        updatedAssessment.setCourse(testCourse);
        updatedAssessment.setTitle("수정된 중간고사");
        updatedAssessment.setScore(88.0f);
        updatedAssessment.setMaxScore(100.0f);
        updatedAssessment.setCreatedAt(LocalDateTime.now());
        updatedAssessment.setUpdatedAt(LocalDateTime.now());

        when(courseAssessmentRepository.updateCourseAssessment(any(CourseAssessment.class)))
                .thenReturn(updatedAssessment);

        // When
        CourseAssessmentOutput output = courseAssessmentService.updateCourseAssessment(input);

        // Then
        assertNotNull(output);
        assertEquals(courseAssessmentId, output.getId());
        assertEquals("수정된 중간고사", output.getTitle());
        assertEquals(88.0f, output.getScore());
        assertEquals(100.0f, output.getMaxScore());

        verify(courseAssessmentRepository).updateCourseAssessment(argThat(courseAssessment ->
                courseAssessment.getId().equals(courseAssessmentId) &&
                courseAssessment.getTitle().equals("수정된 중간고사") &&
                courseAssessment.getScore().equals(88.0f) &&
                courseAssessment.getMaxScore().equals(100.0f)
        ));
    }

    @Test
    @DisplayName("과제 평가 삭제")
    void deleteCourseAssessment() {
        // Given
        doNothing().when(courseAssessmentRepository).deleteCourseAssessment(courseAssessmentId);

        // When
        courseAssessmentService.deleteCourseAssessment(courseAssessmentId);

        // Then
        verify(courseAssessmentRepository).deleteCourseAssessment(courseAssessmentId);
    }
}