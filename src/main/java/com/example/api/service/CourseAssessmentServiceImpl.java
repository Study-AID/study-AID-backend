package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.entity.Course;
import com.example.api.entity.CourseAssessment;
import com.example.api.entity.User;
import com.example.api.repository.CourseAssessmentRepository;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.courseAssessment.*;

@Service
public class CourseAssessmentServiceImpl implements CourseAssessmentService {
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    private CourseAssessmentRepository courseAssessmentRepo;

    @Autowired
    public void CourseAssessmentService(
            UserRepository userRepo,
            CourseRepository courseRepo,
            CourseAssessmentRepository courseAssessmentRepo
    ) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.courseAssessmentRepo = courseAssessmentRepo;
    }

    @Override
    public Optional<CourseAssessmentOutput> findCourseAssessmentById(UUID courseAssessmentId) {
        return courseAssessmentRepo.findById(courseAssessmentId)
                .map(CourseAssessmentOutput::fromEntity);
    }

    @Override
    public CourseAssessmentListOutput findCourseAssessmentsByCourseId(UUID courseId) {
        List<CourseAssessment> courseAssessments = courseAssessmentRepo.findByCourseId(courseId);
        return CourseAssessmentListOutput.fromEntities(courseAssessments);
    }

    @Override
    @Transactional
    public CourseAssessmentOutput createCourseAssessment(CreateCourseAssessmentInput input) {
        User user = userRepo.getReferenceById(input.getUserId());
        Course course = courseRepo.getReferenceById(input.getCourseId());

        CourseAssessment courseAssessment = new CourseAssessment();
        courseAssessment.setCourse(course);
        courseAssessment.setUser(user);
        courseAssessment.setTitle(input.getTitle());
        courseAssessment.setScore(input.getScore());
        courseAssessment.setMaxScore(input.getMaxScore());

        CourseAssessment createdCourseAssessment = courseAssessmentRepo.createCourseAssessment(courseAssessment);
        return CourseAssessmentOutput.fromEntity(createdCourseAssessment);
    }

    @Override
    @Transactional
    public CourseAssessmentOutput updateCourseAssessment(UpdateCourseAssessmentInput input) {
        CourseAssessment courseAssessment = new CourseAssessment();
        courseAssessment.setId(input.getId());
        courseAssessment.setTitle(input.getTitle());
        courseAssessment.setScore(input.getScore());
        courseAssessment.setMaxScore(input.getMaxScore());

        CourseAssessment updatedCourseAssessment = courseAssessmentRepo.updateCourseAssessment(courseAssessment);
        return CourseAssessmentOutput.fromEntity(updatedCourseAssessment);
    }

    @Override
    @Transactional
    public void deleteCourseAssessment(UUID courseAssessmentId) {
        courseAssessmentRepo.deleteCourseAssessment(courseAssessmentId);
    }
}