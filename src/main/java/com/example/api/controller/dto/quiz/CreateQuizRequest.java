package com.example.api.controller.dto.quiz;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "quiz creation request")
public class CreateQuizRequest {
    @NotNull
    @Schema(description = "ID of the lecture")
    private UUID lectureId;

    @NotBlank
    @Schema(description = "Title of the quiz")
    private String title;

    @NotBlank
    @Schema(description = "Parsed text for quiz generation")
    private String parsedText;

    @NotNull
    @Schema(description = "Number of true or false questions")
    private int trueOrFalseCount;

    @NotNull
    @Schema(description = "Number of multiple choice questions")
    private int multipleChoiceCount;

    @NotNull
    @Schema(description = "Number of short answer questions")
    private int shortAnswerCount;

    @NotNull
    @Schema(description = "Number of essay questions")
    private int essayCount;
}
