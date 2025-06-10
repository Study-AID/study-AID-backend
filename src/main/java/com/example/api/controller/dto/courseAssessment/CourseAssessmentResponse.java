package com.example.api.controller.dto.courseAssessment;

import com.example.api.service.dto.courseAssessment.CourseAssessmentOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course assessment response DTO")
public class CourseAssessmentResponse {
    @NotNull
    @Schema(description = "Unique ID of the course assessment")
    private UUID id;

    @NotNull
    @Schema(description = "Unique ID of the course")
    private UUID courseId;

    @NotNull
    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @Schema(description = "Title of the course assessment")
    private String title;

    @NotNull
    @Schema(description = "Score achieved")
    private Float score;

    @NotNull
    @Schema(description = "Maximum possible score")
    private Float maxScore;

    @NotNull
    @Schema(description = "Creation timestamp of the course assessment")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "Last update timestamp of the course assessment")
    private LocalDateTime updatedAt;

    public static CourseAssessmentResponse fromServiceDto(CourseAssessmentOutput courseAssessmentOutput) {
        return new CourseAssessmentResponse(
                courseAssessmentOutput.getId(),
                courseAssessmentOutput.getCourseId(),
                courseAssessmentOutput.getUserId(),
                courseAssessmentOutput.getTitle(),
                courseAssessmentOutput.getScore(),
                courseAssessmentOutput.getMaxScore(),
                courseAssessmentOutput.getCreatedAt(),
                courseAssessmentOutput.getUpdatedAt()
        );
    }
}