package com.example.api.controller.dto.semester;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update semester dates request")
public class UpdateSemesterDatesRequest {
    @Schema(description = "Start date of the semester", example = "2024-03-01")
    private LocalDate startDate;

    @Schema(description = "End date of the semester", example = "2024-06-30")
    private LocalDate endDate;
}
