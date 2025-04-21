package com.example.api.controller.dto.course;

import com.example.api.service.dto.course.CourseOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course response")
public class CourseResponse {
    @Schema(description = "Unique ID of the course")
    private UUID id;

    @Schema(description = "Unique ID of the semester")
    private UUID semesterId;

    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @Schema(description = "Name of the course")
    private String name;

    @Schema(description = "Target grade for the course")
    private Float targetGrade;

    @Schema(description = "Earned grade for the course")
    private Float earnedGrade;

    @Schema(description = "Completed credits for the course")
    private Integer completedCredits;

    @Schema(description = "Creation timestamp of the course")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp of the course")
    private LocalDateTime updatedAt;

    public static CourseResponse fromServiceDto(CourseOutput course) {
        return new CourseResponse(
                course.getId(),
                course.getSemesterId(),
                course.getUserId(),
                course.getName(),
                course.getTargetGrade(),
                course.getEarnedGrade(),
                course.getCompletedCredits(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
