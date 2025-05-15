package com.example.api.controller.dto.quiz;


import java.time.LocalDateTime;
import java.util.List;

import com.example.api.entity.QuizItem;
import com.example.api.entity.enums.Status;
import com.example.api.service.dto.quiz.QuizOutput;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class QuizResponse {
    @Schema(description = "Quiz ID")
    private String id;

    @Schema(description = "Lecture ID")
    private String lectureId;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Quiz title")
    private String title;

    @Schema(description = "Quiz status")
    private Status status;

    @Schema(description = "Contents generation time")
    private LocalDateTime contentsGenerateAt;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time")
    private LocalDateTime updatedAt;

    @Schema(description = "Quiz item list")
    private List<QuizItem> quizItems;
    

    public static QuizResponse fromServiceDto(QuizOutput quiz) {
        return new QuizResponse(
                quiz.getId().toString(),
                quiz.getLectureId().toString(),
                quiz.getUserId().toString(),
                quiz.getTitle(),
                quiz.getStatus(),
                quiz.getContentsGenerateAt(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt(),
                quiz.getQuizItems() // Assuming quizItems is a field in QuizOutput
        );
    }
}