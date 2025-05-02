package com.example.api.controller.dto.lecture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "List of lectures response")
public class LectureListResponse {
    @Schema(description = "List of lectures response")
    private List<LectureResponse> lectures;
}
