package com.example.api.controller.dto.semester;

import com.example.api.entity.enums.Season;
import com.example.api.service.dto.semester.SemesterOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Semester response")
public class SemesterResponse {
    @NotNull
    @Schema(description = "Unique ID of the semester")
    private UUID id;

    @NotNull
    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @NotNull
    @Schema(description = "Name of the semester")
    private String name;

    @NotNull
    @Schema(description = "Year of the semester")
    private int year;

    @NotNull
    @Schema(description = "Season of the semester (SPRING, SUMMER, FALL, WINTER)")
    private Season season;

    @Schema(description = "Start date of the semester")
    private LocalDate startDate;

    @Schema(description = "End date of the semester")
    private LocalDate endDate;

    @Schema(description = "Target grade for the semester")
    private Float targetGrade;

    @Schema(description = "Earned grade for the semester")
    private Float earnedGrade;

    @Schema(description = "Completed credits for the semester")
    private Integer completedCredits;

    @NotNull
    @Schema(description = "Creation timestamp of the semester")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "Last update timestamp of the semester")
    private LocalDateTime updatedAt;

    public static SemesterResponse fromServiceDto(SemesterOutput semester) {
        return new SemesterResponse(
                semester.getId(),
                semester.getUserId(),
                semester.getName(),
                semester.getYear(),
                semester.getSeason(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.getTargetGrade(),
                semester.getEarnedGrade(),
                semester.getCompletedCredits(),
                semester.getCreatedAt(),
                semester.getUpdatedAt()
        );
    }
}
