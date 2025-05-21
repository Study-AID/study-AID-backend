package com.example.api.controller.dto.exam;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.service.dto.exam.ExamResponseOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exam response DTO")
public class SubmitExamResponse {
    @Schema(description = "Exam response ID")
    private UUID id;

    @Schema(description = "Exam ID")
    private UUID examId;

    @Schema(description = "Exam item ID")
    private UUID examItemId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time")
    private LocalDateTime updatedAt;

    public static SubmitExamResponse fromServiceDto(ExamResponseOutput exam) {
        return new SubmitExamResponse(
                exam.getId(),
                exam.getExamId(),
                exam.getExamItemId(),
                exam.getUserId(),
                exam.getCreatedAt(),
                exam.getUpdatedAt()
        );
    }
}
