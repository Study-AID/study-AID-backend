package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.entity.Course;
import com.example.api.entity.Lecture;
import com.example.api.entity.User;
import com.example.api.entity.enums.SummaryStatus;
import com.example.api.repository.CourseRepository;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.lecture.CreateLectureInput;
import com.example.api.service.dto.lecture.LectureListOutput;
import com.example.api.service.dto.lecture.LectureOutput;
import com.example.api.service.dto.lecture.UpdateLectureDisplayOrderLexInput;
import com.example.api.service.dto.lecture.UpdateLectureInput;


@Service
public class LectureServiceImpl implements LectureService {
    // Implement the methods from LectureService interface here
    // For example:
    // @Override
    // public Optional<LectureOutput> findLectureById(UUID lectureId) {
    //     // Implementation here
    // }
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    private LectureRepository lectureRepo;

    @Autowired
    public void LectureService(
            UserRepository userRepo,
            CourseRepository courseRepo,
            LectureRepository lectureRepo
    ) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.lectureRepo = lectureRepo;
    }

    @Override
    public Optional<LectureOutput> findLectureById(UUID lectureId) {
        return lectureRepo.findById(lectureId)
                .map(LectureOutput::fromEntity);
    }

    @Override
    public LectureListOutput findLecturesByCourseId(UUID courseId) {
        List<LectureOutput> lectures = lectureRepo.findByCourseId(courseId)
                .stream()
                .map(LectureOutput::fromEntity)
                .toList();
        return new LectureListOutput(lectures);
    }

    @Override
    @Transactional
    public LectureOutput createLecture(CreateLectureInput input) {
        User user = userRepo.getReferenceById(input.getUserId());
        Course course = courseRepo.getReferenceById(input.getCourseId());

        Lecture lecture = new Lecture();
        lecture.setCourse(course);
        lecture.setUser(user);
        lecture.setTitle(input.getTitle());
        lecture.setMaterialPath(input.getMaterialPath());
        lecture.setMaterialType(input.getMaterialType());
        lecture.setDisplayOrderLex(input.getDisplayOrderLex());
        lecture.setSummaryStatus(SummaryStatus.not_started);

        Lecture createdLecture = lectureRepo.createLecture(lecture);
        return LectureOutput.fromEntity(createdLecture);
    }

    @Override
    @Transactional
    public LectureOutput updateLecture(UpdateLectureInput input) {
        Lecture lecture = new Lecture();
        lecture.setId(input.getId());
        lecture.setTitle(input.getTitle());
        lecture.setMaterialPath(input.getMaterialPath());
        lecture.setMaterialType(input.getMaterialType());

        Lecture updatedLecture = lectureRepo.updateLecture(lecture);
        return LectureOutput.fromEntity(updatedLecture);
    }

    @Override
    @Transactional
    public LectureOutput updateLectureDisplayOrderLex(UpdateLectureDisplayOrderLexInput input) {
        Lecture lecture = new Lecture();
        lecture.setId(input.getId());
        lecture.setDisplayOrderLex(input.getDisplayOrderLex());

        Lecture updatedLecture = lectureRepo.updateLecture(lecture);
        return LectureOutput.fromEntity(updatedLecture);
    }

    @Override
    @Transactional
    public void deleteLecture(UUID lectureId) {
        lectureRepo.deleteLecture(lectureId);
    }
}
