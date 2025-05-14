package com.example.api.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.entity.Lecture;
import com.example.api.entity.Quiz;
import com.example.api.entity.User;
import com.example.api.entity.enums.Status;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QuizRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.quiz.CreateQuizInput;
import com.example.api.service.dto.quiz.QuizListOutput;
import com.example.api.service.dto.quiz.QuizOutput;
import com.example.api.service.dto.quiz.UpdateQuizInput;

import jakarta.transaction.Transactional;

@Service
public class QuizServiceImpl implements QuizService {
    private UserRepository userRepo;
    private QuizRepository quizRepo;
    private LectureRepository lectureRepo;

    @Autowired
    public void QuizService(
            UserRepository userRepo,
            QuizRepository quizRepo,
            LectureRepository lectureRepo
    ) {
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.lectureRepo = lectureRepo;
    }

    @Override
    public Optional<QuizOutput> findQuizById(UUID quizId) {
        return quizRepo.findById(quizId)
                .map(QuizOutput::fromEntity);
    }

    @Override
    public QuizListOutput findQuizzesByLectureId(UUID lectureId) {
        return QuizListOutput.fromEntities(quizRepo.findByLectureId(lectureId));
    }

    @Override
    @Transactional
    public QuizOutput createQuiz(CreateQuizInput input) {
        User user = userRepo.getReferenceById(input.getUserId());
        Lecture lecture = lectureRepo.getReferenceById(input.getLectureId());

        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setLecture(lecture);
        quiz.setTitle(input.getTitle());
        quiz.setStatus(Status.not_started);

        Quiz createdQuiz = quizRepo.createQuiz(quiz);
        return QuizOutput.fromEntity(createdQuiz);
    }

    @Override
    @Transactional
    public QuizOutput updateQuiz(UpdateQuizInput input) {
        Quiz quiz = new Quiz();
        quiz.setId(input.getId());
        quiz.setTitle(input.getTitle());
        quiz.setStatus(input.getStatus());

        Quiz updateQuiz = quizRepo.updateQuiz(quiz);
        return QuizOutput.fromEntity(updateQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID quizId) {
        quizRepo.deleteQuiz(quizId);
    }
}
