package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.entity.Course;
import com.example.api.entity.Exam;
import com.example.api.entity.ExamItem;
import com.example.api.entity.User;
import com.example.api.entity.enums.Status;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.ExamItemRepository;
import com.example.api.repository.ExamRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.exam.CreateExamInput;
import com.example.api.service.dto.exam.ExamListOutput;
import com.example.api.service.dto.exam.ExamOutput;
import com.example.api.service.dto.exam.UpdateExamInput;

@Service
public class ExamServiceImpl implements ExamService {
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    private ExamRepository examRepo;
    private ExamItemRepository examItemRepo;

    @Autowired
    public void ExamService(
            UserRepository userRepo,
            CourseRepository courseRepo,
            ExamRepository examRepo,
            ExamItemRepository examItemRepo
    ) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.examRepo = examRepo;
        this.examItemRepo = examItemRepo;
    }

    @Override
    public Optional<ExamOutput> findExamById(UUID examId) {
        Optional<Exam> exam = examRepo.findById(examId);
        List<ExamItem> examItems = examItemRepo.findByExamId(examId);
        
        return exam.map(e -> ExamOutput.fromEntity(e, examItems));
    }
    
    @Override
    public ExamListOutput findExamsByCourseId(UUID courseId) {
        return ExamListOutput.fromEntities(examRepo.findByCourseId(courseId));
    }

    @Override
    @Transactional
    public ExamOutput createExam(CreateExamInput input) {
        User user = userRepo.getReferenceById(input.getUserId());
        Course course = courseRepo.getReferenceById(input.getCourseId());

        Exam exam = new Exam();
        exam.setUser(user);
        exam.setCourse(course);
        exam.setTitle(input.getTitle());
        exam.setStatus(Status.generate_in_progress);

        Exam createdExam = examRepo.createExam(exam);
        List<ExamItem> examItems = examItemRepo.findByExamId(createdExam.getId());
        if (examItems.isEmpty()) {
            throw new IllegalArgumentException("Exam items cannot be empty");
        }
        return ExamOutput.fromEntity(createdExam, List.of());
    }

    @Override
    @Transactional
    public ExamOutput updateExam(UpdateExamInput input) {
        Exam exam = new Exam();
        exam.setId(input.getId());
        exam.setTitle(input.getTitle());

        Exam updatedExam = examRepo.updateExam(exam);
        List<ExamItem> examItems = examItemRepo.findByExamId(updatedExam.getId());
        return ExamOutput.fromEntity(updatedExam, examItems);
    }

    @Override
    @Transactional
    public void deleteExam(UUID examId) {
        examRepo.deleteExam(examId);
    }
}
