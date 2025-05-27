package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.*;
import com.example.api.service.dto.quiz.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizServiceImpl implements QuizService {
    private UserRepository userRepo;
    private QuizRepository quizRepo;
    private QuizItemRepository quizItemRepo;
    private LectureRepository lectureRepo;

    private QuizResponseRepository quizResponseRepo;
    private QuizResultRepository quizResultRepo;
    private LikedQuizItemRepository likedQuizItemRepo;

    @Autowired
    public void QuizService(
            UserRepository userRepo,
            QuizRepository quizRepo,
            QuizItemRepository quizItemRepo,
            LectureRepository lectureRepo,
            QuizResponseRepository quizResponseRepo,
            QuizResultRepository quizResultRepo,
            LikedQuizItemRepository likedQuizItemRepo
    ) {
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.quizItemRepo = quizItemRepo;
        this.lectureRepo = lectureRepo;
        this.quizResponseRepo = quizResponseRepo;
        this.quizResultRepo = quizResultRepo;
        this.likedQuizItemRepo = likedQuizItemRepo;
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
        return QuizOutput.fromEntity(createdQuiz, List.of());
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

                    // 퀴즈 풀이 저장이 성공했는지 확인
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
            if (quizItem.getQuestionType() == null) {
                throw new IllegalArgumentException("Exam item question type cannot be null");
            }
            if (quizItem.getQuestionType() == QuestionType.true_or_false) {
                // true/false 문제
                if (quizResponse.getSelectedBool() != null && quizResponse.getSelectedBool().equals(quizItem.getIsTrueAnswer())) {
                    // totalScore += quizItem.getPoints();
                    quizResponse.setIsCorrect(true);
                }
            } else if (quizItem.getQuestionType() == QuestionType.multiple_choice) {
                // 객관식 문제 - 순서 상관없이 선택된 항목들이 정답과 일치하는지 확인
                if (quizResponse.getSelectedIndices() != null && quizItem.getAnswerIndices() != null) {
                    Set<Integer> selectedSet = new HashSet<>(Arrays.asList(quizResponse.getSelectedIndices()));
                    Set<Integer> answerSet = new HashSet<>(Arrays.asList(quizItem.getAnswerIndices()));
                    
                    if (selectedSet.equals(answerSet)) {
                        // totalScore += quizItem.getPoints();
                        quizResponse.setIsCorrect(true);
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

    @Override
    @Transactional
    public ToggleLikeQuizItemOutput toggleLikeQuizItem(ToggleLikeQuizItemInput input) {
        // 퀴즈 존재 확인
        Quiz quiz = quizRepo.getReferenceById(input.getQuizId());
        QuizItem quizItem = quizItemRepo.getReferenceById(input.getQuizItemId());
        User user = userRepo.getReferenceById(input.getUserId());

        // 퀴즈 문제가 해당 퀴즈에 속하는지 확인
        if (!quizItem.getQuiz().getId().equals(input.getQuizId())) {
            throw new IllegalArgumentException("Quiz item does not belong to the specified quiz");
        }

        // 기존 좋아요 확인
        Optional<LikedQuizItem> existingLikedQuizItem = likedQuizItemRepo.findByQuizItemIdAndUserId(
                input.getQuizItemId(), input.getUserId());

        boolean isLiked;
        
        if (existingLikedQuizItem.isPresent()) {
            // 좋아요가 이미 존재하면 삭제 (좋아요 취소)
            likedQuizItemRepo.deleteLikedQuizItem(existingLikedQuizItem.get().getId());
            isLiked = false;
        } else {
            // 좋아요가 존재하지 않으면 생성 (좋아요 추가)
            LikedQuizItem newLikedQuizItem = new LikedQuizItem();
            newLikedQuizItem.setQuiz(quiz);
            newLikedQuizItem.setQuizItem(quizItem);
            newLikedQuizItem.setUser(user);
            
            likedQuizItemRepo.createLikedQuizItem(newLikedQuizItem);
            isLiked = true;
        }

        return new ToggleLikeQuizItemOutput(
                input.getQuizId(),
                input.getQuizItemId(),
                input.getUserId(),
                isLiked
        );
    }
}
