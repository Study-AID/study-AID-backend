package com.example.api.service.dto.semester;

import com.example.api.entity.Semester;
import com.example.api.entity.enums.Season;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterOutput {
    private UUID id;
    private UUID userId;
    private String name;
    private int year;
    private Season season;
    private LocalDate startDate;
    private LocalDate endDate;
    private Float targetGrade;
    private Float earnedGrade;
    private Integer completedCredits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SemesterOutput fromEntity(Semester semester) {
        return new SemesterOutput(
                semester.getId(),
                semester.getUser() != null ? semester.getUser().getId() : null,
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
