package com.example.api.controller.dto.exam;

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
@Schema(description = "exam creation request")
public class CreateExamRequest {
    @NotNull
    @Schema(description = "ID of the course")
    private UUID courseId;

    @NotBlank
    @Schema(description = "Title of the exam")
    private String title;

    @NotNull
    @Schema(description = "Referenced lectures for the exam")
    private UUID[] referencedLectures;

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
