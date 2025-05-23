package com.example.api.controller.dto.lecture.summary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Topic details with hierarchical structure")
public class TopicDetails {
    @Schema(description = "Title of the topic")
    private String title;
    
    @Schema(description = "Description of the topic")
    private String description;
    
    @Schema(description = "Page range where topic is covered")
    private PageRange pageRange;
    
    @Schema(description = "Additional details about the topic")
    private List<String> additionalDetails;
    
    @Schema(description = "Sub-topics under this topic")
    private List<TopicDetails> subTopics;
}