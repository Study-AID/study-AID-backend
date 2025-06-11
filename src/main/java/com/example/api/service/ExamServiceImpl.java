package com.example.api.service;

import com.example.api.adapters.sqs.GradeExamEssayMessage;
import com.example.api.adapters.sqs.SQSClient;
import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.*;
import com.example.api.service.dto.exam.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@Slf4j
public class ExamServiceImpl implements ExamService {
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    private ExamRepository examRepo;
    private ExamItemRepository examItemRepo;
    private ExamResponseRepository examResponseRepo;
    private ExamResultRepository examResultRepo;
    private SQSClient sqsClient;

    @Autowired
    public void ExamService(
            UserRepository userRepo,
            CourseRepository courseRepo,
            ExamRepository examRepo,
            ExamItemRepository examItemRepo,
            ExamResponseRepository examResponseRepo,
            ExamResultRepository examResultRepo,
            SQSClient sqsClient
    ) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.examRepo = examRepo;
        this.examItemRepo = examItemRepo;
        this.examResponseRepo = examResponseRepo;
        this.examResultRepo = examResultRepo;
        this.sqsClient = sqsClient;
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
        return ExamOutput.fromEntity(createdExam, List.of());
    }

    @Override
    @Transactional
    public ExamOutput updateExam(UpdateExamInput input) {
        // 기존 Entity 조회 후 필드 업데이트 (new Entity 방식에서 get and set 방식으로 변경)
        Exam exam = examRepo.findById(input.getId())
                .orElseThrow(() -> new RuntimeException("Exam not found: " + input.getId()));
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

    @Override
    @Transactional
    public ExamResponseListOutput submitAndGradeExamWithStatus(List<CreateExamResponseInput> inputs) {
        List<ExamResponseOutput> examResponseOutputs = inputs.stream()
                .map(input -> {
                    Exam exam = examRepo.getReferenceById(input.getExamId());
                    ExamItem examItem = examItemRepo.getReferenceById(input.getExamItemId());
                    User user = userRepo.getReferenceById(input.getUserId());

                    ExamResponse examResponse = new ExamResponse();
                    examResponse.setExam(exam);
                    examResponse.setExamItem(examItem);
                    examResponse.setUser(user);
                    if (input.getSelectedBool() != null) {
                        examResponse.setSelectedBool(input.getSelectedBool());
                    }
                    if (input.getSelectedIndices() != null) {
                        examResponse.setSelectedIndices(input.getSelectedIndices());
                    }
                    if (input.getTextAnswer() != null) {
                        examResponse.setTextAnswer(input.getTextAnswer());
                    }

                    // ExamResponse(퀴즈 풀이이) 생성
                    ExamResponse createdExamResponse = examResponseRepo.createExamResponse(examResponse);

                    // ExamResponse 저장이 성공했는지 확인
                    if (createdExamResponse == null) {
                        throw new RuntimeException("Failed to create exam response");
                    }

                    return ExamResponseOutput.fromEntity(createdExamResponse);
                }).toList();

        ExamResponseListOutput examResponseListOutput = new ExamResponseListOutput(examResponseOutputs);

        UUID examId = inputs.get(0).getExamId();
        Exam exam = examRepo.getReferenceById(examId);
        UUID userId = inputs.get(0).getUserId();
        UUID courseId = exam.getCourse().getId();

        // 서술형 문제 유무 확인
        boolean hasEssay = hasEssayQuestions(examId);
        if (!hasEssay) {
            gradeNonEssayQuestions(examId);
            exam.setStatus(Status.graded);
            sendGenerateCourseWeaknessAnalysisMessage(userId, null, examId, courseId);
        } else {
            gradeNonEssayQuestions(examId);
            exam.setStatus(Status.partially_graded);
            sendGradeExamEssayMessage(userId, examId);
        }
        examRepo.updateExam(exam);

        return examResponseListOutput;
    }

    // 서술형 문제 존재 여부 확인 함수
    private boolean hasEssayQuestions(UUID examId) {
        return examItemRepo.existsByExamIdAndQuestionTypeAndDeletedAtIsNull(examId, QuestionType.essay);
    }

    // 서술형 채점 위한 SQS 메세지 전송 함수
    private void sendGradeExamEssayMessage(UUID userId, UUID examId) {
        GradeExamEssayMessage message = GradeExamEssayMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(userId)
                .examId(examId)
                .build();

        try {
            sqsClient.sendGradeExamEssayMessage(message);
            log.info("Successfully sent grade exam essay message to SQS: examId={}, userId={}", examId, userId);
        } catch (Exception e) {
            log.error("Failed to send grade exam essay message to SQS: examId={}, userId={}, error={}", examId, userId, e.getMessage());
        }
    }

    // 과목 약점 분석 위한 SQS 메시지 전송하는 함수
    private void sendGenerateCourseWeaknessAnalysisMessage(UUID userId, UUID quizId, UUID examId, UUID courseId) {
        GenerateCourseWeaknessAnalysisMessage message = new GenerateCourseWeaknessAnalysisMessage(
                "1.0.0",
                UUID.randomUUID(),
                OffsetDateTime.now(),
                userId,
                quizId,
                examId,
                courseId
        );

        try {
            sqsClient.sendGenerateCourseWeaknessAnalysisMessage(message);
            log.info("Successfully sent generate course weakness analysis message to SQS: courseId={}, userId={}, quizId={}, examId={}",
                    courseId, userId, quizId, examId);
        } catch (Exception e) {
            log.error("Failed to send generate course weakness analysis message to SQS: courseId={}, userId={}, error={}",
                    courseId, userId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void gradeNonEssayQuestions(UUID examId) {
        Exam exam = examRepo.getReferenceById(examId);
        List<ExamResponse> examResponses = examResponseRepo.findByExamId(examId);

        List<ExamItem> examItems = examItemRepo.findByExamId(examId);
        Float initialMaxScore = setPointsForExamItems(examItems);

        float nonEssayScore = 0;
        for (ExamResponse examResponse : examResponses) {
            ExamItem examItem = examItemRepo.getReferenceById(examResponse.getExamItem().getId());
            if (examItem.getQuestionType() == null) {
                throw new IllegalArgumentException("Exam item question type cannot be null");
            }
            // 서술형 문제는 이 함수에서 채점하지 않음
            if(examItem.getQuestionType() == QuestionType.essay) {
                continue;
            }
            if (examItem.getQuestionType() == QuestionType.true_or_false) {
                // true/false 문제에 대한 채점 로직
                if (examResponse.getSelectedBool() != null && examResponse.getSelectedBool().equals(examItem.getIsTrueAnswer())) {
                    nonEssayScore += examItem.getPoints();
                    examResponse.setIsCorrect(true);
                }
            } else if (examItem.getQuestionType() == QuestionType.multiple_choice) {
                // 객관식 문제에 대한 채점 로직 - 순서 상관없이 선택된 항목들이 정답과 일치하는지 확인
                if (examResponse.getSelectedIndices() != null && examItem.getAnswerIndices() != null) {
                    Set<Integer> selectedSet = new HashSet<>(Arrays.asList(examResponse.getSelectedIndices()));
                    Set<Integer> answerSet = new HashSet<>(Arrays.asList(examItem.getAnswerIndices()));

                    if (selectedSet.equals(answerSet)) {
                        nonEssayScore += examItem.getPoints();
                        examResponse.setIsCorrect(true);
                    }
                }
            } else if (examItem.getQuestionType() == QuestionType.short_answer) {
                // 단답형 문제에 대한 채점 로직
                if (examResponse.getTextAnswer() != null && examResponse.getTextAnswer().equals(examItem.getTextAnswer())) {
                    nonEssayScore += examItem.getPoints();
                    examResponse.setIsCorrect(true);
                }
            }
            examResponseRepo.updateExamResponse(examResponse);
        }

        ExamResult examResult = new ExamResult();
        examResult.setExam(exam);
        examResult.setUser(examResponses.get(0).getUser());
        examResult.setScore(nonEssayScore);
        examResult.setMaxScore(initialMaxScore);

        examResultRepo.createExamResult(examResult);
    }
    
    // 각 시험 아이템에 점수를 설정하고 총점을 반환하는 함수
    private Float setPointsForExamItems(List<ExamItem> examItems) {
        Float initialMaxScore = 0f;
        for (ExamItem examItem : examItems) {
            if (examItem.getQuestionType() == QuestionType.true_or_false) {
                examItem.setPoints(1f); // True/False 문제는 1점
                initialMaxScore += 1f;
            } else if (examItem.getQuestionType() == QuestionType.multiple_choice) {
                examItem.setPoints(3f); // 객관식 문제는 3점
                initialMaxScore += 3f;
            } else if (examItem.getQuestionType() == QuestionType.short_answer) {
                examItem.setPoints(5f); // 주관식 문제는 5점
                initialMaxScore += 5f;
            } else if (examItem.getQuestionType() == QuestionType.essay) {
                examItem.setPoints(10f); // 서술형 문제는 10점
                initialMaxScore += 10f;
            } else {
                throw new IllegalArgumentException("Unknown question type: " + examItem.getQuestionType());
            }
            examItemRepo.updateExamItem(examItem);
        }
        return initialMaxScore;
    }
    
    @Override
    @Transactional
    public Optional<ExamResultOutput> findExamResultByExamId(UUID examId) {
        Optional<ExamResult> examResult = examResultRepo.findByExamId(examId);
        List<ExamItem> examItems = examItemRepo.findByExamId(examId);
        List<UUID> examItemIds = examItems.stream().map(ExamItem::getId).toList();
        
        // 각 시험 아이템에 대한 시험 응답을 가져옴
        List<ExamResultElement> examResultElements = new ArrayList<>();
        for (UUID examItemId : examItemIds) {
            Optional<ExamResponse> examResponses = examResponseRepo.findByExamItemId(examItemId);
            if (examResponses.isPresent()) {
                ExamResponse examResponse = examResponses.get();
                ExamItem examItem = examItemRepo.getReferenceById(examItemId);
                ExamResultElement element = ExamResultElement.fromExamItemAndResponse(examItem, examResponse);
                examResultElements.add(element);
            }
        }
        
        return examResult.map(er -> ExamResultOutput.fromEntityAndExamResultElements(er, examResultElements));
    }

    @Override
    @Transactional
    public ExamResultListOutput findExamResultsByCourseId(UUID courseId) {
        List<Exam> exams = examRepo.findByCourseId(courseId);
        List<ExamResult> examResults = new ArrayList<>();
        
        for (Exam exam : exams) {
            Optional<ExamResult> result = examResultRepo.findByExamId(exam.getId());
            if (result.isPresent()) {
                examResults.add(result.get());
            }
        }
        
        ExamResultListOutput examResultOutputs = ExamResultListOutput.fromEntities(examResults);
        return examResultOutputs;
    }

    @Override
    @Transactional
    public Float calculateExamAverageScore(UUID courseId) {
        List<Exam> exams = examRepo.findByCourseId(courseId);
        float totalScore = 0;
        int count = 0;

        for (Exam exam : exams) {
            Optional<ExamResult> result = examResultRepo.findByExamId(exam.getId());
            if (result.isPresent()) {
                ExamResult examResult = result.get();
                Float eachScore = examResult.getScore() / examResult.getMaxScore() * 100; // Convert to percentage
                totalScore += eachScore;
                count++;
            }
        }

        return count > 0 ? totalScore / count : 0f;
    }

    @Override
    public ExamItemListOutput findLikedExamItemByCourseId(UUID courseId) {
        List<Exam> exams = examRepo.findByCourseId(courseId);
        if (exams.isEmpty()) {
            return new ExamItemListOutput(Collections.emptyList());
        }

        List<UUID> examIds = exams.stream()
                .map(Exam::getId)
                .toList();

        List<ExamItemOutput> examItemOutputs = new ArrayList<>();
        for (UUID examId : examIds) {
            List<ExamItem> examItems = examItemRepo.findByExamId(examId);
            if (examItems.isEmpty()) {
                continue;
            }
            for (ExamItem examItem : examItems) {
                if (examItem.getIsLiked() != null && examItem.getIsLiked()) {
                    ExamItemOutput examItemOutput = ExamItemOutput.fromEntity(examItem);
                    examItemOutputs.add(examItemOutput);
                }
            }
        }

        return new ExamItemListOutput(examItemOutputs);
    }

    @Override
    @Transactional
    public ExamItemOutput toggleLikeExamItem(ToggleLikeExamItemInput input) {
        Optional<ExamItem> existingExamItem = examItemRepo.findById(input.getExamItemId());
        if (existingExamItem.isEmpty()) {
            throw new NoSuchElementException("Exam item not found");
        }

        if (existingExamItem.get().getIsLiked() != null && existingExamItem.get().getIsLiked()) {
            // 이미 좋아요가 눌려져 있다면 좋아요 취소
            existingExamItem.get().setIsLiked(false);
        } else {
            // 좋아요가 눌려져 있지 않다면 좋아요 추가
            existingExamItem.get().setIsLiked(true);
        }

        ExamItem updatedExamItem = examItemRepo.updateExamItem(existingExamItem.get());
        if (updatedExamItem == null) {
            throw new RuntimeException("Failed to update exam item like status");
        }
        return ExamItemOutput.fromEntity(updatedExamItem);
    }
}
