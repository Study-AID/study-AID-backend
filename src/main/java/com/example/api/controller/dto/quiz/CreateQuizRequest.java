package com.example.api.controller.dto.quiz;

import java.util.UUID;

import com.example.api.entity.enums.Status;

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
    @Schema(description = "Status of the quiz")
    private Status status;
}
