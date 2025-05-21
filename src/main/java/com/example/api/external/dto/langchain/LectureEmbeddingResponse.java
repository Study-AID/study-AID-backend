package com.example.api.external.dto.langchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureEmbeddingResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("lecture_id")
    private String lectureId;

    @JsonProperty("total_chunks")
    private int totalChunks;

    @JsonProperty("sample_vectors")
    private float[] sampleVectors;
}
