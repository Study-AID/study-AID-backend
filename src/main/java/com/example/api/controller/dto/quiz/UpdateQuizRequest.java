package com.example.api.controller.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Quiz update request")
public class UpdateQuizRequest {
    @NotBlank
    @Schema(description = "Updated quiz title")
    private String title;
}
