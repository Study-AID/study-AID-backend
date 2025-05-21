package com.example.api.controller.dto.quiz;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Quiz response submission request")
public class CreateQuizResponseRequest {
    @NotBlank
    @Schema(description = "ID of the quiz item")
    private UUID quizItemId;

    @NotBlank
    @Schema(description = "Selected answer for OX questions")
    private Boolean selectedBool;

    @NotBlank
    @Schema(description = "Selected answers for multiple-choice questions")
    private Integer[] selectedIndices;

    @NotBlank
    @Schema(description = "Text answer for text questions")
    private String textAnswer;
}
