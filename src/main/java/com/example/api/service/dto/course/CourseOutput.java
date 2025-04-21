package com.example.api.service.dto.course;

import com.example.api.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseOutput {
    private UUID id;
    private UUID userId;
    private UUID semesterId;
    private String name;
    private Float targetGrade;
    private Float earnedGrade;
    private Integer completedCredits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Convert from entity to DTO
    public static CourseOutput fromEntity(Course course) {
        CourseOutput dto = new CourseOutput();
        dto.setId(course.getId());
        dto.setUserId(course.getUser() != null ? course.getUser().getId() : null);
        dto.setSemesterId(course.getSemester() != null ? course.getSemester().getId() : null);
        dto.setName(course.getName());
        dto.setTargetGrade(course.getTargetGrade());
        dto.setEarnedGrade(course.getEarnedGrade());
        dto.setCompletedCredits(course.getCompletedCredits());
        dto.setCreatedAt(course.getCreatedAt() != null ? course.getCreatedAt() : null);
        dto.setUpdatedAt(course.getUpdatedAt() != null ? course.getUpdatedAt() : null);
        return dto;
    }
}