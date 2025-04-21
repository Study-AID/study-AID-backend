package com.example.api.service.dto.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateCourseGradesInput {
    private UUID id;
    private float targetGrade;
    private float earnedGrade;
    private int completedCredits;
}