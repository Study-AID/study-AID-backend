package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Course update request")
public class UpdateCourseRequest {
    private String name;
}
