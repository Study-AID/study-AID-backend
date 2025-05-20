package com.example.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.api.entity.Lecture;
import com.example.api.entity.Quiz;
import com.example.api.entity.QuizItem;
import com.example.api.entity.QuizResponse;
import com.example.api.entity.QuizResult;
import com.example.api.entity.User;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QuizItemRepository;
import com.example.api.repository.QuizRepository;
import com.example.api.repository.QuizResponseRepository;
import com.example.api.repository.QuizResultRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.quiz.CreateQuizInput;
import com.example.api.service.dto.quiz.CreateQuizResponseInput;
import com.example.api.service.dto.quiz.QuizListOutput;
import com.example.api.service.dto.quiz.QuizOutput;
import com.example.api.service.dto.quiz.QuizResponseListOutput;
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
    private QuizResultRepository quizResultRepo;

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
        quiz.setStatus(Status.generate_in_progress);

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

        Quiz updateQuiz = quizRepo.updateQuiz(quiz);
        List<QuizItem> quizItems = quizItemRepo.findByQuizId(updateQuiz.getId());
        return QuizOutput.fromEntity(updateQuiz, quizItems);
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID quizId) {
        quizRepo.deleteQuiz(quizId);
    }

    @Override
    @Transactional
    public QuizResponseListOutput createQuizResponse(List<CreateQuizResponseInput> inputs) {
        QuizResponseListOutput quizResponseListOutput = (QuizResponseListOutput) inputs.stream()
                .map(input -> {
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
                    
                    return QuizResponseOutput.fromEntity(createdQuizResponse);
                }).toList();
        Quiz quiz = quizRepo.getReferenceById(inputs.get(0).getQuizId());
        quiz.setStatus(Status.submitted);
        quizRepo.updateQuiz(quiz);
        return quizResponseListOutput;
    }

    @Override
    @Transactional
    public void gradeQuiz(UUID quizId) {
        // 퀴즈 풀이와 퀴즈 아이템을 가져와서 답을 비교하여 점수 부여
        Quiz quiz = quizRepo.getReferenceById(quizId);
        List<QuizResponse> quizResponses = quizResponseRepo.findByQuizId(quizId);

        // 퀴즈 아이템의 questionType에 문제 유형을 확인하여 점수를 부여
        float totalScore = 0;
        for (QuizResponse quizResponse : quizResponses) {
            QuizItem quizItem = quizItemRepo.getReferenceById(quizResponse.getQuizItem().getId());
            if (quizItem.getQuestionType() == QuestionType.true_or_false) {
                // true/false 문제
                if (quizResponse.getSelectedBool() != null && quizResponse.getSelectedBool().equals(quizItem.getIsTrueAnswer())) {
                    // totalScore += quizItem.getPoints();
                    quizResponse.setIsCorrect(true);
                }
            } else if (quizItem.getQuestionType() == QuestionType.multiple_choice) {
                // 객관식 문제
                if (quizResponse.getSelectedIndices() != null && quizResponse.getSelectedIndices().length > 0) {
                    for (int index : quizResponse.getSelectedIndices()) {
                        if (index < quizItem.getAnswerIndices().length && quizItem.getAnswerIndices()[index].equals(quizItem.getAnswerIndices()[index])) {
                            // totalScore += quizItem.getPoints();
                            quizResponse.setIsCorrect(true);
                        }
                    }
                }
            } else if (quizItem.getQuestionType() == QuestionType.short_answer) {
                // 주관식 문제
                if (quizResponse.getTextAnswer() != null && quizResponse.getTextAnswer().equals(quizItem.getTextAnswer())) {
                    // totalScore += quizItem.getPoints();
                    quizResponse.setIsCorrect(true);
                }
            }
            // TODO(jin): 서술형 문제 구현


            // // 점수 부여
            // if (quizResponse.getIsCorrect()) {
            //     quizResponse.setScore(quizItem.getPoints());
            //     totalScore += quizItem.getPoints();
            // } else {
            //     quizResponse.setScore(0f);
            // }
            quizResponseRepo.updateQuizResponse(quizResponse);
        }
        quiz.setStatus(Status.graded);
        
        quizRepo.updateQuiz(quiz);

        // 퀴즈 결과 생성 
        // TODO(yoon) : 퀴즈 결과 생성 로직을 별개의 메서드로 분리
        QuizResult quizResult = new QuizResult();
        quizResult.setQuiz(quiz);
        quizResult.setUser(quizResponses.get(0).getUser());
        quizResult.setScore(totalScore);
        quizResult.setMaxScore(totalScore);
        quizResult.setFeedback("Feedback");
        quizResult.setStartTime(quizResponses.get(0).getCreatedAt());
        quizResult.setEndTime(LocalDateTime.now());
        quizResultRepo.createQuizResult(quizResult);
    }
}
