package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(description = "Course creation request")
public class CreateCourseRequest {
    private UUID semesterId;
    private String name;
}
