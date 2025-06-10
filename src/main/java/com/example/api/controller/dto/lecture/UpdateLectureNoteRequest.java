package com.example.api.controller.dto.lecture;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Lecture note update request")
public class UpdateLectureNoteRequest {
    @NotBlank
    @Schema(description = "Updated lecture note content")
    private String note;    
    // String으로 request 받고 컨트롤러 코드에서 Map<String, String>으로 변환하여 서비스로 전달
}
