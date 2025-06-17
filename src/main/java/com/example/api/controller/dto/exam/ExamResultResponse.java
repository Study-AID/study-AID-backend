package com.example.api.controller.dto.exam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.api.service.dto.exam.ExamResultElement;
import com.example.api.service.dto.exam.ExamResultOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exam Result response DTO")
public class ExamResultResponse { 
    @Schema(description = "Exam Result ID")
    private UUID id;

    @Schema(description = "Exam ID")
    private UUID examId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Score")
    private Float score;

    @Schema(description = "Maximum Score")
    private Float maxScore;

    @Schema(description = "Start Time")
    private LocalDateTime startTime;

    @Schema(description = "End Time")
    private LocalDateTime endTime;

    @Schema(description = "Creation Time")
    private LocalDateTime createdAt;

    @Schema(description = "Last Update Time")
    private LocalDateTime updatedAt;

    @Schema(description = "Elements of the Exam Result (Exam Item, Exam Response)")
    private List<ExamResultElement> examResultElements;

    public static ExamResultResponse fromServiceDto(ExamResultOutput examResultOutput) {
        return new ExamResultResponse(
                examResultOutput.getId(),
                examResultOutput.getExamId(),
                examResultOutput.getUserId(),
                examResultOutput.getScore(),
                examResultOutput.getMaxScore(),
                examResultOutput.getStartTime(),
                examResultOutput.getEndTime(),
                examResultOutput.getCreatedAt(),
                examResultOutput.getUpdatedAt(),
                examResultOutput.getExamResultElements()
        );
    }

}
