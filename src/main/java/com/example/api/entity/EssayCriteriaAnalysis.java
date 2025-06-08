package com.example.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayCriteriaAnalysis {
    private List<ScoringCriterion> criteria;

    @JsonProperty("student_answer_analysis")
    private String studentAnswerAnalysis;
}
