package com.example.api.service.dto.courseAssessment;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCourseAssessmentInput {
    private UUID courseId;
    private UUID userId;
    private String title;
    private Float score;
    private Float maxScore;
}