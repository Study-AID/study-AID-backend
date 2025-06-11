package com.example.api.service.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.api.entity.ExamResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultOutput {
    private UUID id;
    private UUID examId;
    private UUID userId;
    private Float score;
    private Float maxScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ExamResultElement> examResultElements;

    public static ExamResultOutput fromEntity(ExamResult examResult) {
        return new ExamResultOutput(
                examResult.getId(),
                examResult.getExam().getId(),
                examResult.getUser().getId(),
                examResult.getScore(),
                examResult.getMaxScore(),
                examResult.getStartTime(),
                examResult.getEndTime(),
                examResult.getCreatedAt(),
                examResult.getUpdatedAt(),
                null // No elements provided here
        );
    }

    public static ExamResultOutput fromEntityAndExamResultElements(ExamResult examResult, List<ExamResultElement> examResultElements) {
        return new ExamResultOutput(
                examResult.getId(),
                examResult.getExam().getId(),
                examResult.getUser().getId(),
                examResult.getScore(),
                examResult.getMaxScore(),
                examResult.getStartTime(),
                examResult.getEndTime(),
                examResult.getCreatedAt(),
                examResult.getUpdatedAt(),
                examResultElements
        );
    }
}
