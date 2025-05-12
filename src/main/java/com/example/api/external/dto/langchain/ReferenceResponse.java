package com.example.api.external.dto.langchain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReferenceResponse {
    private List<ReferenceChunkResponse> references;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceChunkResponse {
        private String text;
        private int page;
    }
}