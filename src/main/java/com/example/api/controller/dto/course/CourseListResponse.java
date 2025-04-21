package com.example.api.controller.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of courses response")
public class CourseListResponse {
    @Schema(description = "List of course responses")
    private List<CourseResponse> courses;
}
