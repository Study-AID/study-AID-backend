package com.example.api.external.dto.langchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class EmbeddingCheckResponse {
    @JsonProperty("directory_exists")
    private boolean directoryExists;

    @JsonProperty("loadable")
    private boolean loadable;

    @JsonProperty("vector_count")
    private int vectorCount;

    public boolean isVectorized() {
        return directoryExists && loadable && vectorCount > 0;
    }
}