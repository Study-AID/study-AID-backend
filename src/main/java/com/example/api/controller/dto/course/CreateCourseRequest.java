package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course creation request")
public class CreateCourseRequest {
    @NotNull
    @Schema(description = "ID of the semester")
    private UUID semesterId;

    @NotBlank
    @Schema(description = "Name of the course")
    private String name;
}
