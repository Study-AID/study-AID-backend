package com.example.api.controller.dto.lecture;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lecture creation request")
public class CreateLectureRequest {
    @NotNull
    @Schema(description = "ID of the course")
    private UUID courseId;
}
