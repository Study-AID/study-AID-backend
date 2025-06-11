package com.example.api.service;

import com.example.api.entity.Course;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.course.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {
    private UserRepository userRepo;
    private SemesterRepository semesterRepo;
    private CourseRepository courseRepo;

    @Autowired
    public void CourseService(
            UserRepository userRepo,
            SemesterRepository semesterRepo,
            CourseRepository courseRepo
    ) {
        this.userRepo = userRepo;
        this.semesterRepo = semesterRepo;
        this.courseRepo = courseRepo;
    }

    @Override
    public Optional<CourseOutput> findCourseById(UUID courseId) {
        return courseRepo.findById(courseId)
                .map(CourseOutput::fromEntity);
    }

    @Override
    public CourseListOutput findCoursesBySemesterId(UUID semesterId) {
        List<CourseOutput> courses = courseRepo.findBySemesterId(semesterId)
                .stream()
                .map(CourseOutput::fromEntity)
                .toList();
        return new CourseListOutput(courses);
    }

    @Override
    @Transactional
    public CourseOutput createCourse(CreateCourseInput input) {
        User user = userRepo.getReferenceById(input.getUserId());
        Semester semester = semesterRepo.getReferenceById(input.getSemesterId());

        Course course = new Course();
        course.setUser(user);
        course.setSemester(semester);
        course.setName(input.getName());

        Course createdCourse = courseRepo.createCourse(course);
        return CourseOutput.fromEntity(createdCourse);
    }

    @Override
    @Transactional
    public CourseOutput updateCourse(UpdateCourseInput input) {
        // 기존 Entity 조회 후 필드 업데이트 (new Entity 방식에서 get and set 방식으로 변경)
        Course course = courseRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Course not found: " + input.getId()));
        course.setName(input.getName());
        Course updatedCourse = courseRepo.updateCourse(course);
        return CourseOutput.fromEntity(updatedCourse);
    }

    @Override
    @Transactional
    public CourseOutput updateCourseGrades(UpdateCourseGradesInput input) {
        // 기존 Entity 조회 후 필드 업데이트 (new Entity 방식에서 get and set 방식으로 변경)
        Course course = courseRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Course not found: " + input.getId()));
        course.setTargetGrade(input.getTargetGrade());
        course.setEarnedGrade(input.getEarnedGrade());
        course.setCompletedCredits(input.getCompletedCredits());
        Course updatedCourse = courseRepo.updateCourse(course);
        return CourseOutput.fromEntity(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        courseRepo.deleteCourse(courseId);
    }
}
