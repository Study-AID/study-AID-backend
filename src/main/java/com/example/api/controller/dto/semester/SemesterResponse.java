package com.example.api.controller.dto.semester;

import com.example.api.entity.enums.Season;
import com.example.api.service.dto.semester.SemesterOutput;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Unique ID of the semester")
    private UUID id;

    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @Schema(description = "Name of the semester")
    private String name;

    @Schema(description = "Year of the semester")
    private int year;

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

    @Schema(description = "Creation timestamp of the semester")
    private LocalDateTime createdAt;

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
