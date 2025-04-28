package com.example.api.controller.dto.semester;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create semester request")
public class CreateSemesterRequest {
    @Schema(description = "Name of the semester", example = "Spring 2024")
    private String name;

    @Schema(description = "Year of the semester", example = "2024")
    private int year;

    @Schema(description = "Season of the semester (spring, summer,)", example = "SPRING")
    private String season;
}
