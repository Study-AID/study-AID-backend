package com.example.api.controller.dto.lecture;

import com.example.api.entity.enums.SummaryStatus;
import com.example.api.service.dto.lecture.LectureOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lecture response DTO")
public class LectureResponse {
    @NotNull
    @Schema(description = "Unique ID of the lecture")
    private UUID id;

    @NotNull
    @Schema(description = "Unique ID of the course")
    private UUID courseId;

    @NotNull
    @Schema(description = "Unique ID of the user")
    private UUID userId;

    @NotNull
    @Schema(description = "Title of the lecture")
    private String title;

    @Schema(description = "Path to the lecture material")
    private String materialPath;

    @Schema(description = "Type of the lecture material")
    private String materialType;

    @Schema(description = "Display order lexicographically")
    private String displayOrderLex;

    @Schema(description = "Notes associated with the lecture")
    private Map<String, Object> note;

    @Schema(description = "Summary of the lecture")
    private Map<String, Object> summary;

    @Schema(description = "Summary status of the lecture")
    private SummaryStatus summaryStatus;

    @NotNull
    @Schema(description = "Creation timestamp of the lecture")
    private LocalDateTime createdAt;

    @NotNull
    @Schema(description = "Last update timestamp of the lecture")
    private LocalDateTime updatedAt;

    public static LectureResponse fromServiceDto(LectureOutput lecture) {
        return new LectureResponse(
                lecture.getId(),
                lecture.getCourseId(),
                lecture.getUserId(),
                lecture.getTitle(),
                lecture.getMaterialPath(),
                lecture.getMaterialType(),
                lecture.getDisplayOrderLex(),
                lecture.getNote(),
                lecture.getSummary(),
                lecture.getSummaryStatus(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}
