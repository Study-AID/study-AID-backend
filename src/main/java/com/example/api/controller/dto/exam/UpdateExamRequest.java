package com.example.api.controller.dto.exam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Exam update request")
public class UpdateExamRequest {
    @NotBlank
    @Schema(description = "Updated exam title")
    private String title;
}
