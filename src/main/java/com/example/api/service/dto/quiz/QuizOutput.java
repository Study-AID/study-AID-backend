package com.example.api.service.dto.quiz;

import com.example.api.entity.Quiz;
import com.example.api.entity.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizOutput {
    private UUID id;
    private UUID lectureId;
    private UUID userId;
    private String title;
    private Status status;
    private LocalDateTime contentsGenerateAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizOutput fromEntity(Quiz quiz) {
        return new QuizOutput(
                quiz.getId(),
                quiz.getLecture().getId(),
                quiz.getUser().getId(),
                quiz.getTitle(),
                quiz.getStatus(),
                quiz.getContentsGenerateAt(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }
}
