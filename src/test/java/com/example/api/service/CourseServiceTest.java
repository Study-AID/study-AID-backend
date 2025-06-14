package com.example.api.service;

import com.example.api.entity.Course;
import com.example.api.entity.CourseWeaknessAnalysis;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.course.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CourseServiceTest {
    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private UUID userId;
    private UUID semesterId;
    private UUID courseId;
    private User testUser;
    private Semester testSemester;
    private Course testCourse;
    private CourseOutput testCourseOutput;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        semesterId = UUID.randomUUID();
        courseId = UUID.randomUUID();

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

        testCourseOutput = CourseOutput.fromEntity(testCourse);
    }

    @Test
    @DisplayName("ID로 과목 조회")
    void findCourseById() {
        // Given
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

        // When
        Optional<CourseOutput> output = courseService.findCourseById(courseId);

        // Then
        assertTrue(output.isPresent());
        assertEquals(testCourseOutput.getId(), output.get().getId());
        assertEquals(testCourseOutput.getName(), output.get().getName());
        verify(courseRepository).findById(courseId);
    }

    @Test
    @DisplayName("학기 ID로 과목 목록 조회")
    void findCoursesBySemesterId() {
        // Given
        when(courseRepository.findBySemesterId(semesterId)).thenReturn(Arrays.asList(testCourse));

        // When
        CourseListOutput output = courseService.findCoursesBySemesterId(semesterId);

        // Then
        assertThat(output.getCourses()).hasSize(1);
        assertEquals(testCourseOutput.getId(), output.getCourses().get(0).getId());
        assertEquals(testCourseOutput.getName(), output.getCourses().get(0).getName());
        verify(courseRepository).findBySemesterId(semesterId);
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 분석 데이터 있음")
    void findCourseWeaknessAnalysis_WithData() {
        // Given
        CourseWeaknessAnalysis weaknessAnalysis = new CourseWeaknessAnalysis();
        weaknessAnalysis.setWeaknesses("프로그래밍 기초 개념이 부족합니다.");
        weaknessAnalysis.setSuggestions("기본기를 다시 한번 점검해보세요.");
        weaknessAnalysis.setAnalyzedAt(LocalDateTime.now());

        testCourse.setCourseWeaknessAnalysis(weaknessAnalysis);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

        // When
        CourseWeaknessAnalysis result = courseService.findCourseWeaknessAnalysis(courseId);

        // Then
        assertNotNull(result);
        assertEquals("프로그래밍 기초 개념이 부족합니다.", result.getWeaknesses());
        assertEquals("기본기를 다시 한번 점검해보세요.", result.getSuggestions());
        assertNotNull(result.getAnalyzedAt());
        verify(courseRepository).findById(courseId);
    }

    @Test
    @DisplayName("과목 약점 분석 조회 - 분석 데이터 없음")
    void findCourseWeaknessAnalysis_NoData() {
        // Given
        testCourse.setCourseWeaknessAnalysis(null);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

        // When
        CourseWeaknessAnalysis result = courseService.findCourseWeaknessAnalysis(courseId);

        // Then
        assertNull(result);
        verify(courseRepository).findById(courseId);
    }

    @Test
    @DisplayName("과목 생성")
    void createCourse() {
        // Given
        CreateCourseInput input = new CreateCourseInput();
        input.setUserId(userId);
        input.setSemesterId(semesterId);
        input.setName("운영체제");

        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(semesterRepository.getReferenceById(semesterId)).thenReturn(testSemester);
        when(courseRepository.createCourse(any(Course.class))).thenReturn(testCourse);

        // When
        CourseOutput createdCourse = courseService.createCourse(input);

        // Then
        assertNotNull(createdCourse);
        assertEquals(testCourseOutput.getId(), createdCourse.getId());
        assertEquals(testCourseOutput.getName(), createdCourse.getName());
        verify(userRepository).getReferenceById(userId);
        verify(semesterRepository).getReferenceById(semesterId);
        verify(courseRepository).createCourse(any(Course.class));
    }

    @Test
    @DisplayName("과목 생성 - 중복 과목 예외 발생")
    void createCourse_DuplicateCourse() {
        // Given
        CreateCourseInput input = new CreateCourseInput();
        input.setUserId(userId);
        input.setSemesterId(semesterId);
        input.setName("운영체제");

        when(userRepository.getReferenceById(userId)).thenReturn(testUser);
        when(semesterRepository.getReferenceById(semesterId)).thenReturn(testSemester);
        when(courseRepository.createCourse(any(Course.class)))
                .thenThrow(new InvalidDataAccessApiUsageException("Course with the same name already exists in this semester"));

        // When/Then
        Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            courseService.createCourse(input);
        });
        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository).getReferenceById(userId);
        verify(semesterRepository).getReferenceById(semesterId);
        verify(courseRepository).createCourse(any(Course.class));
    }

    @Test
    @DisplayName("과목 이름 업데이트")
    void updateCourse() {
        // Given
        UpdateCourseInput input = new UpdateCourseInput();
        input.setId(courseId);
        input.setName("고급 운영체제");

        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setUser(testUser);
        updatedCourse.setSemester(testSemester);
        updatedCourse.setName("고급 운영체제");
        updatedCourse.setTargetGrade(testCourse.getTargetGrade());
        updatedCourse.setEarnedGrade(testCourse.getEarnedGrade());
        updatedCourse.setCompletedCredits(testCourse.getCompletedCredits());
        updatedCourse.setCreatedAt(testCourse.getCreatedAt());
        updatedCourse.setUpdatedAt(LocalDateTime.now());

        CourseOutput updatedCourseOutput = CourseOutput.fromEntity(updatedCourse);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(courseRepository.updateCourse(any(Course.class))).thenReturn(updatedCourse);

        // When
        CourseOutput result = courseService.updateCourse(input);

        // Then
        assertEquals(updatedCourseOutput.getId(), result.getId());
        assertEquals(updatedCourseOutput.getName(), result.getName());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).updateCourse(argThat(course ->
                course.getId().equals(courseId) &&
                        course.getName().equals("고급 운영체제")
        ));
    }

    @Test
    @DisplayName("과목 성적 정보 업데이트")
    void updateCourseGrades() {
        // Given
        UpdateCourseGradesInput input = new UpdateCourseGradesInput();
        input.setId(courseId);
        input.setTargetGrade(4.0f);
        input.setEarnedGrade(3.5f);
        input.setCompletedCredits(3);

        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setUser(testUser);
        updatedCourse.setSemester(testSemester);
        updatedCourse.setName(testCourse.getName());
        updatedCourse.setTargetGrade(4.0f);
        updatedCourse.setEarnedGrade(3.5f);
        updatedCourse.setCompletedCredits(3);
        updatedCourse.setCreatedAt(testCourse.getCreatedAt());
        updatedCourse.setUpdatedAt(LocalDateTime.now());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
        when(courseRepository.updateCourse(any(Course.class))).thenReturn(updatedCourse);

        // When
        courseService.updateCourseGrades(input);

        // Then
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).updateCourse(argThat(course ->
                course.getId().equals(courseId) &&
                        course.getTargetGrade() == 4.0f &&
                        course.getEarnedGrade() == 3.5f &&
                        course.getCompletedCredits() == 3
        ));
    }

    @Test
    @DisplayName("과목 삭제")
    void deleteCourse() {
        // Given
        doNothing().when(courseRepository).deleteCourse(courseId);

        // When
        courseService.deleteCourse(courseId);

        // Then
        verify(courseRepository).deleteCourse(courseId);
    }
}
