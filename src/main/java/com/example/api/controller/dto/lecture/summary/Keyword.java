package com.example.api.controller.dto.lecture.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Keyword information with relevance")
public class Keyword {
    @Schema(description = "Keyword text")
    private String keyword;
    
    @Schema(description = "Description of the keyword")
    private String description;
    
    @Schema(description = "Relevance score (0.0 to 1.0)")
    private float relevance;
    
    @Schema(description = "Page range where keyword appears")
    private PageRange pageRange;
}