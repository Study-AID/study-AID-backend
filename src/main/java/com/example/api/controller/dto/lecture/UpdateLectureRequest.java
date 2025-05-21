package com.example.api.controller.dto.lecture;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Lecture update request")
public class UpdateLectureRequest {
    @NotBlank  
    @Schema(description = "Updated lecture title")
    private String title;
}
