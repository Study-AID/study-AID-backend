package com.example.api.controller.dto.lecture.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete lecture summary")
public class Summary {
    @Schema(description = "Metadata about the summary generation")
    private Metadata metadata;
    
    @Schema(description = "Overall overview of the lecture")
    private String overview;
    
    @Schema(description = "Key keywords extracted from the lecture")
    private List<Keyword> keywords;
    
    @Schema(description = "Main topics covered in the lecture")
    private List<TopicDetails> topics;
    
    @Schema(description = "Additional references mentioned in the lecture")
    private List<String> additionalReferences;
}