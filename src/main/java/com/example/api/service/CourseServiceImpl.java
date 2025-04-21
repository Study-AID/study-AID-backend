package com.example.api.service;

import com.example.api.entity.Course;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.course.CourseOutput;
import com.example.api.service.dto.course.CreateCourseInput;
import com.example.api.service.dto.course.UpdateCourseGradesInput;
import com.example.api.service.dto.course.UpdateCourseInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<CourseOutput> findCoursesByUserId(UUID userId) {
        return courseRepo.findByUserId(userId)
                .stream()
                .map(CourseOutput::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseOutput> findCoursesBySemesterId(UUID semesterId) {
        return courseRepo.findBySemesterId(semesterId)
                .stream()
                .map(CourseOutput::fromEntity)
                .collect(Collectors.toList());
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
        Course course = new Course();
        course.setId(input.getId());
        course.setName(input.getName());
        Course updatedCourse = courseRepo.updateCourse(course);
        return CourseOutput.fromEntity(updatedCourse);
    }

    @Override
    @Transactional
    public void updateCourseGrades(UpdateCourseGradesInput input) {
        Course course = new Course();
        course.setId(input.getId());
        course.setTargetGrade(input.getTargetGrade());
        course.setEarnedGrade(input.getEarnedGrade());
        course.setCompletedCredits(input.getCompletedCredits());
        courseRepo.updateCourse(course);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        courseRepo.deleteCourse(courseId);
    }
}