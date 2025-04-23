package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Course grade update request")
public class UpdateCourseGradesRequest {
    @Schema(description = "Updated target grade for the course")
    private float targetGrade;

    @Schema(description = "Updated earned grade for the course")
    private float earnedGrade;

    @Schema(description = "Updated completed credits for the course")
    private int completedCredits;
}
