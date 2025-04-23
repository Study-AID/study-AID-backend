package com.example.api.service.dto.course;

import com.example.api.entity.Course;
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
    private UUID semesterId;
    private UUID userId;
    private String name;
    private Float targetGrade;
    private Float earnedGrade;
    private Integer completedCredits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseOutput fromEntity(Course course) {
        return new CourseOutput(
                course.getId(),
                course.getSemester().getId(),
                course.getUser().getId(),
                course.getName(),
                course.getTargetGrade(),
                course.getEarnedGrade(),
                course.getCompletedCredits(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
