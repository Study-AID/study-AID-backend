package com.example.api.service;

import com.example.api.adapters.sqs.GradeQuizEssayMessage;
import com.example.api.adapters.sqs.SQSClient;
import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.*;
import com.example.api.service.dto.quiz.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Slf4j
public class QuizServiceImpl implements QuizService {
    private UserRepository userRepo;
    private QuizRepository quizRepo;
    private QuizItemRepository quizItemRepo;
    private LectureRepository lectureRepo;

    private QuizResponseRepository quizResponseRepo;
    private QuizResultRepository quizResultRepo;
    private SQSClient sqsClient;

    @Autowired
    public void QuizService(
            UserRepository userRepo,
            QuizRepository quizRepo,
            QuizItemRepository quizItemRepo,
            LectureRepository lectureRepo,
            QuizResponseRepository quizResponseRepo,
            QuizResultRepository quizResultRepo,
            SQSClient sqsClient
    ) {
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
        this.quizItemRepo = quizItemRepo;
        this.lectureRepo = lectureRepo;
        this.quizResponseRepo = quizResponseRepo;
        this.quizResultRepo = quizResultRepo;
        this.sqsClient = sqsClient;
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
    public QuizResponseListOutput submitAndGradeQuizWithStatus(List<CreateQuizResponseInput> inputs) {
        List<QuizResponseOutput> quizResponseOutputs  = inputs.stream()
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
        
        QuizResponseListOutput quizResponseListOutput = new QuizResponseListOutput(quizResponseOutputs);
    
        UUID quizId = inputs.get(0).getQuizId();
        UUID userId = inputs.get(0).getUserId();
        Quiz quiz = quizRepo.getReferenceById(quizId);

        // 퀴즈 상태 'submitted'로 업데이트
        quiz.setStatus(Status.submitted);
        quizRepo.updateQuiz(quiz);

        // 서술형 문제 유무 확인
        boolean hasEssay = hasEssayQuestions(quizId);
        if (!hasEssay) {
            gradeNonEssayQuestions(quizId);
            quiz.setStatus(Status.graded);
        } else {
            gradeNonEssayQuestions(quizId);
            quiz.setStatus(Status.partially_graded);
            sendGradeQuizEssayMessage(userId, quizId);
        }
        quizRepo.updateQuiz(quiz);

        return quizResponseListOutput;
    }

    // 서술형 문제 존재 여부 확인 함수
    private boolean hasEssayQuestions(UUID quizId) {
        return quizItemRepo.existsByQuizIdAndQuestionTypeAndDeletedAtIsNull(quizId, QuestionType.essay);
    }

    @Override
    @Transactional
    public void gradeNonEssayQuestions(UUID quizId) {
        // 퀴즈 풀이와 퀴즈 아이템을 가져와서 답을 비교하여 점수 부여
        Quiz quiz = quizRepo.getReferenceById(quizId);
        List<QuizResponse> quizResponses = quizResponseRepo.findByQuizId(quizId);

        // 퀴즈 총점 설정 및 계산
        List<QuizItem> quizItems = quizItemRepo.findByQuizId(quizId);
        Float initialMaxScore = setPointsForQuizItems(quizItems);

        // 퀴즈 아이템의 questionType에 문제 유형을 확인하여 점수를 부여
        Float nonEssayScore = 0f;
        for (QuizResponse quizResponse : quizResponses) {
            QuizItem quizItem = quizItemRepo.getReferenceById(quizResponse.getQuizItem().getId());
            if (quizItem.getQuestionType() == null) {
                throw new IllegalArgumentException("Exam item question type cannot be null");
            }
            // 서술형 문제는 이 함수에서 채점하지 않음
            if(quizItem.getQuestionType() == QuestionType.essay) {
                continue;
            }
            if (quizItem.getQuestionType() == QuestionType.true_or_false) {
                // true/false 문제
                if (quizResponse.getSelectedBool() != null && quizResponse.getSelectedBool().equals(quizItem.getIsTrueAnswer())) {
                    nonEssayScore += quizItem.getPoints();
                    quizResponse.setIsCorrect(true);
                }
            } else if (quizItem.getQuestionType() == QuestionType.multiple_choice) {
                // 객관식 문제 - 순서 상관없이 선택된 항목들이 정답과 일치하는지 확인
                if (quizResponse.getSelectedIndices() != null && quizItem.getAnswerIndices() != null) {
                    Set<Integer> selectedSet = new HashSet<>(Arrays.asList(quizResponse.getSelectedIndices()));
                    Set<Integer> answerSet = new HashSet<>(Arrays.asList(quizItem.getAnswerIndices()));
                    
                    if (selectedSet.equals(answerSet)) {
                        nonEssayScore += quizItem.getPoints();
                        quizResponse.setIsCorrect(true);
                    }
                }
            } else if (quizItem.getQuestionType() == QuestionType.short_answer) {
                // 주관식 문제
                if (quizResponse.getTextAnswer() != null && quizResponse.getTextAnswer().equals(quizItem.getTextAnswer())) {
                    nonEssayScore += quizItem.getPoints();
                    quizResponse.setIsCorrect(true);
                }
            }
            quizResponseRepo.updateQuizResponse(quizResponse);
        }

        QuizResult quizResult = new QuizResult();
        quizResult.setQuiz(quiz);
        quizResult.setUser(quizResponses.get(0).getUser());
        quizResult.setScore(nonEssayScore);
        quizResult.setMaxScore(initialMaxScore);

        quizResultRepo.createQuizResult(quizResult);
    }

    // 각 퀴즈 아이템에 점수를 설정하고 총점을 반환하는 함수
    private Float setPointsForQuizItems(List<QuizItem> quizItems) {
        Float initialMaxScore = 0f;
        for (QuizItem quizItem : quizItems) {
            if (quizItem.getQuestionType() == QuestionType.true_or_false) {
                quizItem.setPoints(1f); // True/False 문제는 1점
                initialMaxScore += 1f;
            } else if (quizItem.getQuestionType() == QuestionType.multiple_choice) {
                quizItem.setPoints(3f); // 객관식 문제는 3점
                initialMaxScore += 3f;
            } else if (quizItem.getQuestionType() == QuestionType.short_answer) {
                quizItem.setPoints(5f); // 주관식 문제는 5점
                initialMaxScore += 5f;
            } else if (quizItem.getQuestionType() == QuestionType.essay) {
                quizItem.setPoints(10f); // 서술형 문제는 10점
                initialMaxScore += 10f;
            } else {
                throw new IllegalArgumentException("Unknown question type: " + quizItem.getQuestionType());
            }
            quizItemRepo.updateQuizItem(quizItem);
        }
        return initialMaxScore;
    }

    private void sendGradeQuizEssayMessage(UUID userId, UUID quizId) {
        GradeQuizEssayMessage message = GradeQuizEssayMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(userId)
                .quizId(quizId)
                .build();

        try {
            sqsClient.sendGradeQuizEssayMessage(message);
            log.info("Successfully sent grade quiz essay message to SQS: quizId={}, userId={}", quizId, userId);
        } catch (Exception e) {
            log.error("Failed to send grade quiz essay message to SQS: quizId={}, userId={}, error={}", quizId, userId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public Optional<QuizResultOutput> findQuizResultByQuizId(UUID quizId) {
        Optional<QuizResult> quizResult = quizResultRepo.findByQuizId(quizId);
        List<QuizItem> quizItems = quizItemRepo.findByQuizId(quizId);
        List<UUID> quizItemIds = quizItems.stream().map(QuizItem::getId).toList();
        // 각 퀴즈 아이템에 대한 퀴즈 응답을 가져옴
        List<QuizResultElement> quizResultElements = new ArrayList<>();
        for (UUID quizItemId : quizItemIds) {
            Optional<QuizResponse> quizResponses = quizResponseRepo.findByQuizItemId(quizItemId);
            if (quizResponses.isPresent()) {
                QuizResponse quizResponse = quizResponses.get();
                QuizItem quizItem = quizItemRepo.getReferenceById(quizItemId);
                QuizResultElement element = QuizResultElement.fromQuizItemAndResponse(quizItem, quizResponse);
                quizResultElements.add(element);
            }
        }

        return quizResult.map(qr -> QuizResultOutput.fromEntityAndQuizResultElements(qr, quizResultElements));
    }

    @Override
    @Transactional
    public QuizResultListOutput findQuizResultsByCourseId(UUID courseId) {
        List<Lecture> lectures = lectureRepo.findByCourseId(courseId);
        List<QuizResult> quizResults = new ArrayList<>();

        for (Lecture lecture : lectures) {
            List<Quiz> quizzes = quizRepo.findByLectureId(lecture.getId());
            for (Quiz quiz : quizzes) {
                Optional<QuizResult> result = quizResultRepo.findByQuizId(quiz.getId());
                if (result.isPresent()) {
                    quizResults.add(result.get());
                }
            }
        }

        QuizResultListOutput quizResultOutputs = QuizResultListOutput.fromEntities(quizResults);
        return quizResultOutputs;
    }

    @Override
    @Transactional
    public Float calculateQuizAverageScore(UUID courseId) {
        List<Lecture> lectures = lectureRepo.findByCourseId(courseId);
        float totalScore = 0;
        int count = 0;

        for (Lecture lecture : lectures) {
            List<Quiz> quizzes = quizRepo.findByLectureId(lecture.getId());
            for (Quiz quiz : quizzes) {
                Optional<QuizResult> result = quizResultRepo.findByQuizId(quiz.getId());
                if (result.isPresent()) {
                    QuizResult quizResult = result.get();
                    Float eachScore = quizResult.getScore() / quizResult.getMaxScore() * 100; // Convert to percentage
                    totalScore += eachScore;
                    count++;
                }
            }
        }

        return count > 0 ? totalScore / count : 0f;
    }
}
