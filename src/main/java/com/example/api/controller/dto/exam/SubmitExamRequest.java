package com.example.api.controller.dto.exam;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Exam responses submission request")
public class SubmitExamRequest {
    @NotBlank
    @Schema(description = "Exam responses of user")
    private List<SubmitExamItem> submitExamItems;
}
