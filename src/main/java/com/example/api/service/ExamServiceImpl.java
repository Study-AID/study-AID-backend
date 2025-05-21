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
    public ExamResponseListOutput createExamResponse(List<CreateExamResponseInput> inputs) {
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
        Exam exam = examRepo.getReferenceById(inputs.get(0).getExamId());
        exam.setStatus(Status.submitted);
        examRepo.updateExam(exam);
        return examResponseListOutput;
    }

    @Override
    @Transactional
    public void gradeExam(UUID examId) {
        Exam exam = examRepo.getReferenceById(examId);
        List<ExamResponse> examResponses = examResponseRepo.findByExamId(examId);

        float totalScore = 0;
        for (ExamResponse examResponse : examResponses) {
            ExamItem examItem = examItemRepo.getReferenceById(examResponse.getExamItem().getId());
            if (examItem.getQuestionType() == null) {
                throw new IllegalArgumentException("Exam item question type cannot be null");
            }
            if (examItem.getQuestionType() == QuestionType.true_or_false) {
                // true/false 문제에 대한 채점 로직
                if (examResponse.getSelectedBool() != null && examResponse.getSelectedBool().equals(examItem.getIsTrueAnswer())) {
                    // totalScore += examItem.getScore();
                    examResponse.setIsCorrect(true);
                }
            } else if (examItem.getQuestionType() == QuestionType.multiple_choice) {
                // 객관식 문제에 대한 채점 로직 - 순서 상관없이 선택된 항목들이 정답과 일치하는지 확인
                if (examResponse.getSelectedIndices() != null && examItem.getAnswerIndices() != null) {
                    Set<Integer> selectedSet = new HashSet<>(Arrays.asList(examResponse.getSelectedIndices()));
                    Set<Integer> answerSet = new HashSet<>(Arrays.asList(examItem.getAnswerIndices()));

                    if (selectedSet.equals(answerSet)) {
                        // totalScore += examItem.getScore();
                        examResponse.setIsCorrect(true);
                    }
                }
            } else if (examItem.getQuestionType() == QuestionType.short_answer) {
                // 단답형 문제에 대한 채점 로직
                if (examResponse.getTextAnswer() != null && examResponse.getTextAnswer().equals(examItem.getTextAnswer())) {
                    examResponse.setIsCorrect(true);
                }
            }
            // TODO(jin): 서술형 문제 구현

            examResponseRepo.updateExamResponse(examResponse);
        }
        exam.setStatus(Status.graded);
        examRepo.updateExam(exam);

        // 시험 결과 생성 
        // TODO(yoon) : 시험 결과 생성 로직을 별개의 메서드로 분리
        ExamResult examResult = new ExamResult();
        examResult.setExam(exam);
        examResult.setUser(examResponses.get(0).getUser());
        examResult.setScore(totalScore);
        examResult.setMaxScore(totalScore);
        examResult.setFeedback("Feedback");
        examResult.setStartTime(examResponses.get(0).getCreatedAt());
        examResult.setEndTime(LocalDateTime.now());
        examResultRepo.createExamResult(examResult);
    }
}
