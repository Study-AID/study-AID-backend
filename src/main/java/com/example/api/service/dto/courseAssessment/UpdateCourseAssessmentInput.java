package com.example.api.service.dto.courseAssessment;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCourseAssessmentInput {
    private UUID id;
    private String title;
    private Float score;
    private Float maxScore;
}