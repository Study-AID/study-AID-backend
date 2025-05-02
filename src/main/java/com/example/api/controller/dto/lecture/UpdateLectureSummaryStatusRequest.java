package com.example.api.controller.dto.lecture;

import com.example.api.entity.enums.SummaryStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Lecture summary status update request")
public class UpdateLectureSummaryStatusRequest {
    @NotBlank
    @Schema(description = "Updated lecture summary status")
    private SummaryStatus summaryStatus;
}
