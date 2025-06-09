package com.example.api.service.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.api.entity.Exam;
import com.example.api.entity.ExamItem;
import com.example.api.entity.enums.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamOutput {
    private UUID id;
    private UUID courseId;
    private UUID userId;
    private String title;
    private Status status;
    private UUID[] referencedLectures;
    private LocalDateTime contentsGenerateAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ExamItem> examItems;

    public static ExamOutput fromEntity(Exam exam, List<ExamItem> examItems) {
        return new ExamOutput(
                exam.getId(),
                exam.getCourse().getId(),
                exam.getUser().getId(),
                exam.getTitle(),
                exam.getStatus(),
                exam.getReferencedLectures(),
                exam.getContentsGenerateAt(),
                exam.getCreatedAt(),
                exam.getUpdatedAt(), examItems
        );
    }
}
