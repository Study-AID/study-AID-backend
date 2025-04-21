package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Course grade update request")
public class UpdateCourseGradesRequest {
    private float targetGrade;
    private float earnedGrade;
    private int completedCredits;
}
