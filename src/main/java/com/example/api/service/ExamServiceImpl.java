package com.example.api.service;

import com.example.api.entity.*;
import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;
import com.example.api.repository.*;
import com.example.api.service.dto.exam.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamServiceImpl implements ExamService {
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    private ExamRepository examRepo;
    private ExamItemRepository examItemRepo;
    private ExamResponseRepository examResponseRepo;
    private ExamResultRepository examResultRepo;

    @Autowired
    public void ExamService(
            UserRepository userRepo,
            CourseRepository courseRepo,
            ExamRepository examRepo,
            ExamItemRepository examItemRepo,
            ExamResponseRepository examResponseRepo,
            ExamResultRepository examResultRepo
    ) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.examRepo = examRepo;
        this.examItemRepo = examItemRepo;
        this.examResponseRepo = examResponseRepo;
        this.examResultRepo = examResultRepo;
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

    @Override
    @Transactional
    public ExamResponseListOutput submitAndGradeExamWithStatus(List<CreateExamResponseInput> inputs) {
        ExamResponseListOutput examResponseListOutput = (ExamResponseListOutput) inputs.stream()
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
        UUID examId = inputs.get(0).getExamId();
        Exam exam = examRepo.getReferenceById(examId);

        // 모의시험 상태 'submitted'로 업데이트
        exam.setStatus(Status.submitted);
        examRepo.updateExam(exam);

        // 서술형 문제 유무 확인
        boolean hasEssay = hasEssayQuestions(examId);
        if (!hasEssay) {
            gradeNonEssayQuestions(examId);
            exam.setStatus(Status.graded);
        } else {
            gradeNonEssayQuestions(examId);
            exam.setStatus(Status.partially_graded);
            // TODO(jin): 서술형 채점 함수 (gradeEssayQuestions) 작성 및 서술형 채점 SQS 메시지 전송
        }
        examRepo.updateExam(exam);

        return examResponseListOutput;
    }

    // 서술형 문제 존재 여부 확인 함수
    private boolean hasEssayQuestions(UUID examId) {
        return examItemRepo.existsByExamIdAndQuestionTypeAndDeletedAtIsNull(examId, QuestionType.essay);
    }

    @Override
    @Transactional
    public void gradeNonEssayQuestions(UUID examId) {
        Exam exam = examRepo.getReferenceById(examId);
        List<ExamResponse> examResponses = examResponseRepo.findByExamId(examId);

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
                    // nonEssayScore += examItem.getScore();
                    examResponse.setIsCorrect(true);
                }
            } else if (examItem.getQuestionType() == QuestionType.multiple_choice) {
                // 객관식 문제에 대한 채점 로직 - 순서 상관없이 선택된 항목들이 정답과 일치하는지 확인
                if (examResponse.getSelectedIndices() != null && examItem.getAnswerIndices() != null) {
                    Set<Integer> selectedSet = new HashSet<>(Arrays.asList(examResponse.getSelectedIndices()));
                    Set<Integer> answerSet = new HashSet<>(Arrays.asList(examItem.getAnswerIndices()));

                    if (selectedSet.equals(answerSet)) {
                        // nonEssayScore += examItem.getScore();
                        examResponse.setIsCorrect(true);
                    }
                }
            } else if (examItem.getQuestionType() == QuestionType.short_answer) {
                // 단답형 문제에 대한 채점 로직
                if (examResponse.getTextAnswer() != null && examResponse.getTextAnswer().equals(examItem.getTextAnswer())) {
                    examResponse.setIsCorrect(true);
                }
            }
            examResponseRepo.updateExamResponse(examResponse);
        }

        ExamResult examResult = new ExamResult();
        examResult.setExam(exam);
        examResult.setUser(examResponses.get(0).getUser());
        examResult.setScore(nonEssayScore);
        examResult.setMaxScore(nonEssayScore);
        examResult.setFeedback("Feedback");
        examResult.setStartTime(examResponses.get(0).getCreatedAt());
        examResult.setEndTime(LocalDateTime.now());
        examResultRepo.createExamResult(examResult);
        
        exam.setStatus(Status.graded);
        
        examRepo.updateExam(exam);
    }

    @Override
    public Optional<ExamResultOutput> findExamResultByExamId(UUID examId) {
        Optional<ExamResult> examResult = examResultRepo.findByExamId(examId);
        return examResult.map(ExamResultOutput::fromEntity);
    }
    
    @Override
    public ExamResultListOutput findExamResultsByCourseId(UUID courseId) {
        return ExamResultListOutput.fromEntities(examResultRepo.findByCourseId(courseId));
    }
}
