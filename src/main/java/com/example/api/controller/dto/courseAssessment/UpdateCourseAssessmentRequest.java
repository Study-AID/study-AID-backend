package com.example.api.controller.dto.courseAssessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course assessment update request")
public class UpdateCourseAssessmentRequest {
    @NotNull
    @Schema(description = "Title of the course assessment")
    private String title;

    @NotNull
    @Schema(description = "Score achieved")
    private Float score;

    @NotNull
    @Schema(description = "Maximum possible score")
    private Float maxScore;
}