package com.example.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringCriterion {
    private String name;
    private String description;

    @JsonProperty("max_points")
    private Double maxPoints;

    @JsonProperty("earned_points")
    private Double earnedPoints;
}
