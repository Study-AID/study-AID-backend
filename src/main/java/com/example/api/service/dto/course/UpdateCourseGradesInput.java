package com.example.api.service.dto.course;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UpdateCourseGradesInput {
    private UUID id;
    private Float targetGrade;
    private Float earnedGrade;
    private Integer completedCredits;
}
