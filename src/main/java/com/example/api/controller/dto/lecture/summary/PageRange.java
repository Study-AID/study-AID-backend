package com.example.api.controller.dto.lecture.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Page range information")
public class PageRange {
    @Schema(description = "Starting page number")
    private int startPage;
    
    @Schema(description = "Ending page number")
    private int endPage;
}