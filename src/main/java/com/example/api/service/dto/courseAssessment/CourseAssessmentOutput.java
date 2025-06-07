package com.example.api.service.dto.courseAssessment;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.api.entity.CourseAssessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAssessmentOutput {
    private UUID id;
    private UUID courseId;
    private UUID userId;
    private String title;
    private Float score;
    private Float maxScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseAssessmentOutput fromEntity(CourseAssessment courseAssessment) {
        return new CourseAssessmentOutput(
                courseAssessment.getId(),
                courseAssessment.getCourse().getId(),
                courseAssessment.getUser().getId(),
                courseAssessment.getTitle(),
                courseAssessment.getScore(),
                courseAssessment.getMaxScore(),
                courseAssessment.getCreatedAt(),
                courseAssessment.getUpdatedAt()
        );
    }
}