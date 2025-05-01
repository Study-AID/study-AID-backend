package com.example.api.controller.dto.semester;

import com.example.api.entity.enums.Season;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update semester request")
public class UpdateSemesterRequest {
    @Schema(description = "Name of the semester", example = "Spring 2024")
    private String name;

    @Schema(description = "Year of the semester", example = "2024")
    private int year;

    @Schema(description = "Season of the semester (SPRING, SUMMER, FALL, WINTER)", example = "SPRING")
    private String season;
}
