package com.example.api.service.dto.exam;

import com.example.api.entity.ExamResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseOutput {
    private UUID id;
    private UUID examId;
    private UUID examItemId;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExamResponseOutput fromEntity(ExamResponse examResponse) {
        return new ExamResponseOutput(
                examResponse.getId(),
                examResponse.getExam().getId(),
                examResponse.getExamItem().getId(),
                examResponse.getUser().getId(),
                examResponse.getCreatedAt(),
                examResponse.getUpdatedAt()
        );
    }
}
