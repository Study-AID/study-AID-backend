package com.example.api.external.dto.langchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureEmbeddingResponse {
    private String message;

    @JsonProperty("lecture_id")
    private String lectureId;

    private int totalChunks;

    private float[] sampleVectors;
}
