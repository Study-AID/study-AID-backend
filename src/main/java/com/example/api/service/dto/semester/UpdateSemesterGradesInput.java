package com.example.api.service.dto.semester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSemesterGradesInput {
    private UUID id;
    private float targetGrade;
    private float earnedGrade;
    private int completedCredits;
}