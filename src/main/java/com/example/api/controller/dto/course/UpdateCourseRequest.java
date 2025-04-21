package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Course update request")
public class UpdateCourseRequest {
    @NotBlank
    @Schema(description = "Updated course name")
    private String name;
}
