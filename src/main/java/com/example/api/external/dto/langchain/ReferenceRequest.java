package com.example.api.external.dto.langchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReferenceRequest {
    private String question;
    @JsonProperty("max_num_references")
    private int maxNumReferences;
    @JsonProperty("min_similarity")
    private double minSimilarity;
}