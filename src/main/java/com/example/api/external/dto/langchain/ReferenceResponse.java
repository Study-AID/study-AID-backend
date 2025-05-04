package com.example.api.external.dto.langchain;

import lombok.Getter;

import java.util.List;

@Getter
public class ReferenceResponse {
    private List<ReferenceChunkResponse> references;

    @Getter
    public static class ReferenceChunkResponse {
        private String text;
        private int page;
    }
}
