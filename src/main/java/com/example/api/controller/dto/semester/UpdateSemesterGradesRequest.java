package com.example.api.controller.dto.semester;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update semester grades request")
public class UpdateSemesterGradesRequest {
    @Schema(description = "Target grade for the semester", example = "4.0")
    private float targetGrade;

    @Schema(description = "Earned grade for the semester", example = "3.5")
    private float earnedGrade;

    @Schema(description = "Completed credits for the semester", example = "15")
    private int completedCredits;
}
