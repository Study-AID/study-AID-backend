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
    private UUID id;
    private UUID semesterId;
    private UUID userId;
    private String name;
    private Float targetGrade;
    private Float earnedGrade;
    private Integer completedCredits;
    private LocalDateTime createdAt;
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
