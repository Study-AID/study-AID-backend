package com.example.api.controller.dto.courseAssessment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of course assessments response")
public class CourseAssessmentListResponse {
    @Schema(description = "List of course assessments")
    private List<CourseAssessmentResponse> courseAssessments;
}