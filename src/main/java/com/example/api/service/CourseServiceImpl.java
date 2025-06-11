package com.example.api.service;

import com.example.api.entity.Course;
import com.example.api.entity.CourseWeaknessAnalysis;
import com.example.api.entity.CourseAssessment;
import com.example.api.entity.Semester;
import com.example.api.entity.User;
import com.example.api.repository.CourseAssessmentRepository;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.SemesterRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.course.*;
import jakarta.persistence.EntityNotFoundException;
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
    private CourseAssessmentRepository courseAssessmentRepo;

    @Autowired
    public void CourseService(
            UserRepository userRepo,
            SemesterRepository semesterRepo,
            CourseRepository courseRepo,
            CourseAssessmentRepository courseAssessmentRepo
    ) {
        this.userRepo = userRepo;
        this.semesterRepo = semesterRepo;
        this.courseRepo = courseRepo;
        this.courseAssessmentRepo = courseAssessmentRepo;
    }

    @Override
    public Optional<CourseOutput> findCourseById(UUID courseId) {
        Optional<CourseOutput> course = courseRepo.findById(courseId).map(CourseOutput::fromEntity);
        if (course.get().getEarnedGrade() == null) {
            List<CourseAssessment> courseAssessments = courseAssessmentRepo.findByCourseId(courseId);
            if (courseAssessments.isEmpty()) {
                return course;
            }
            float totalScore = 0f;
            float totalMaxScore = 0f;
            for (CourseAssessment assessment : courseAssessments) {
                totalScore += assessment.getScore();
                totalMaxScore += assessment.getMaxScore();
            }
            float earnedGrade = totalMaxScore > 0 ? (totalScore / totalMaxScore) * 100 : 0f;
            if (earnedGrade > 90f)
                course.get().setEarnedGrade(4.5f);
            else if (earnedGrade > 80f)
                course.get().setEarnedGrade(4.0f);
            else if (earnedGrade > 70f)
                course.get().setEarnedGrade(3.5f);
            else if (earnedGrade > 60f)
                course.get().setEarnedGrade(3.0f);
            else if (earnedGrade > 50f)
                course.get().setEarnedGrade(2.5f);
            else if (earnedGrade > 40f)
                course.get().setEarnedGrade(2.0f);
            else if (earnedGrade > 30f)
                course.get().setEarnedGrade(1.5f);
            else if (earnedGrade > 20f)
                course.get().setEarnedGrade(1.0f);
            else
                course.get().setEarnedGrade(0.0f);
        }
        return course;
    }

    @Override
    public CourseWeaknessAnalysis findCourseWeaknessAnalysis(UUID courseId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));

        return course.getCourseWeaknessAnalysis();
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

        if (input.getTargetGrade() != null) {
            course.setTargetGrade(input.getTargetGrade());
        }
        if (input.getEarnedGrade() != null) {
            course.setEarnedGrade(input.getEarnedGrade());
        }
        if (input.getCompletedCredits() != null) {
            course.setCompletedCredits(input.getCompletedCredits());
        }

        Course updatedCourse = courseRepo.updateCourse(course);
        return CourseOutput.fromEntity(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        courseRepo.deleteCourse(courseId);
    }
}
