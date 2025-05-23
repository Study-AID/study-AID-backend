package com.example.api.controller.dto.lecture.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metadata for lecture summary")
public class Metadata {
    @Schema(description = "Model used for generating summary")
    private String model;
    
    @Schema(description = "Creation timestamp")
    private String createdAt;
}