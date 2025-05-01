package com.example.api.controller.dto.lecture;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.example.api.entity.enums.SummaryStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lecture creation request")
public class CreateLectureRequest {
    @NotNull
    @Schema(description = "ID of the course")
    private UUID courseId;

    @NotBlank
    @Schema(description = "Title of the lecture")
    private String title;

    @NotBlank
    @Schema(description = "Path to the lecture material")
    private String materialPath;

    @NotBlank
    @Schema(description = "Type of the lecture material")
    private String materialType;

    @NotBlank
    @Schema(description = "Display order in lexicographical order")
    private String displayOrderLex;

    @NotNull
    @Schema(description = "Summary status of the lecture")
    private SummaryStatus summaryStatus;
}
