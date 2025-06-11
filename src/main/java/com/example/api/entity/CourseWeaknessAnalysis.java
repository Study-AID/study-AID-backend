package com.example.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseWeaknessAnalysis {

    @JsonProperty("weaknesses")
    private String weaknesses;

    @JsonProperty("suggestions")
    private String suggestions;

    @JsonProperty("analyzed_at")
    private LocalDateTime analyzedAt;
}