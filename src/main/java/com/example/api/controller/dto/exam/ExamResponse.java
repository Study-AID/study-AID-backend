package com.example.api.controller.dto.exam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.api.entity.ExamItem;
import com.example.api.entity.enums.Status;
import com.example.api.service.dto.exam.ExamOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exam response DTO")
public class ExamResponse {
    @Schema(description = "Exam ID")
    private UUID id;

    @Schema(description = "Course ID")
    private UUID courseId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Exam title")
    private String title;

    @Schema(description = "Exam status")
    private Status status;

    @Schema(description = "Referenced lectures")
    private UUID[] referencedLectures;

    @Schema(description = "Contents generation time")
    private LocalDateTime contentsGenerateAt;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time")
    private LocalDateTime updatedAt;

    @Schema(description = "Exam item list")
    private List<ExamItem> examItems;

    public static ExamResponse fromServiceDto(ExamOutput exam) {
        return new ExamResponse(
                exam.getId(),
                exam.getCourseId(),
                exam.getUserId(),
                exam.getTitle(),
                exam.getStatus(),
                exam.getReferencedLectures(),
                exam.getContentsGenerateAt(),
                exam.getCreatedAt(),
                exam.getUpdatedAt(),
                exam.getExamItems() // Assuming examItems is a field in ExamOutput
        );
    }
}
