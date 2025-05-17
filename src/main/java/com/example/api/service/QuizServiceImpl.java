package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.entity.Lecture;
import com.example.api.entity.Quiz;
import com.example.api.entity.QuizItem;
import com.example.api.entity.QuizResponse;
import com.example.api.entity.User;
import com.example.api.entity.enums.Status;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QuizItemRepository;
import com.example.api.repository.QuizRepository;
import com.example.api.repository.QuizResponseRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.quiz.CreateQuizInput;
import com.example.api.service.dto.quiz.CreateQuizResponseInput;
import com.example.api.service.dto.quiz.QuizListOutput;
import com.example.api.service.dto.quiz.QuizOutput;
import com.example.api.service.dto.quiz.QuizResponseOutput;
import com.example.api.service.dto.quiz.UpdateQuizInput;

import jakarta.transaction.Transactional;

@Service
public class QuizServiceImpl implements QuizService {
    private UserRepository userRepo;
    private QuizRepository quizRepo;
    private QuizItemRepository quizItemRepo;
    private LectureRepository lectureRepo;

    private QuizResponseRepository quizResponseRepo;

    @Autowired
    public void QuizService(
            UserRepository userRepo,
            QuizRepository quizRepo,
            QuizItemRepository quizItemRepo,
            LectureRepository lectureRepo,
            QuizResponseRepository quizResponseRepo
    ) {
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.quizItemRepo = quizItemRepo;
        this.lectureRepo = lectureRepo;
        this.quizResponseRepo = quizResponseRepo;
    }

    @Override
    public Optional<QuizOutput> findQuizById(UUID quizId) {
        Optional<Quiz> quiz = quizRepo.findById(quizId);
        List<QuizItem> quizItems = quizItemRepo.findByQuizId(quizId);
        
        return quiz.map(q -> QuizOutput.fromEntity(q, quizItems));
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
        List<QuizItem> quizItems = quizItemRepo.findByQuizId(createdQuiz.getId());
        return QuizOutput.fromEntity(createdQuiz, quizItems);
    }

    @Override
    @Transactional
    public QuizOutput updateQuiz(UpdateQuizInput input) {
        Quiz quiz = new Quiz();
        quiz.setId(input.getId());
        quiz.setTitle(input.getTitle());
        quiz.setStatus(input.getStatus());

        Quiz updateQuiz = quizRepo.updateQuiz(quiz);
        List<QuizItem> quizItems = quizItemRepo.findByQuizId(updateQuiz.getId());
        return QuizOutput.fromEntity(updateQuiz, quizItems);
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID quizId) {
        quizRepo.deleteQuiz(quizId);
    }

    // 퀴즈 풀이 저장 - QuizResponse create
    public QuizResponseOutput createQuizResponse(CreateQuizResponseInput input) {
        Quiz quiz = quizRepo.getReferenceById(input.getQuizId());
        QuizItem quizItem = quizItemRepo.getReferenceById(input.getQuizItemId());
        User user = userRepo.getReferenceById(input.getUserId());

        QuizResponse quizResponse = new QuizResponse();
        quizResponse.setQuiz(quiz);
        quizResponse.setQuizItem(quizItem);
        quizResponse.setUser(user);
        // selectedBool, selectedIndices, textAnswer을 set하는데 null일 수 있음.
        // null일 경우에는 set하지 않음.
        if (input.getSelectedBool() != null) {
            quizResponse.setSelectedBool(input.getSelectedBool());
        }
        if (input.getSelectedIndices() != null) {
            quizResponse.setSelectedIndices(input.getSelectedIndices());
        }
        if (input.getTextAnswer() != null) {
            quizResponse.setTextAnswer(input.getTextAnswer());
        }
        // 퀴즈 풀이를 생성
        QuizResponse createdQuizResponse = quizResponseRepo.createQuizResponse(quizResponse);

        // 퀴즈 풀이 저장이 성공했는지 확인한 후, 퀴즈의 상태를 submitted로 변경
        if (createdQuizResponse == null) {
            throw new RuntimeException("Failed to create quiz response");
        }
        quiz.setStatus(Status.submitted);
        quizRepo.updateQuiz(quiz);
        return QuizResponseOutput.fromEntity(createdQuizResponse);
    }


    @Override
    @Transactional
    public void gradeQuiz(UUID quizId) {
        Quiz quiz = quizRepo.getReferenceById(quizId);
        quiz.setStatus(Status.submitted);
        quizRepo.updateQuiz(quiz);

        quiz.setStatus(Status.graded);
    }
}
