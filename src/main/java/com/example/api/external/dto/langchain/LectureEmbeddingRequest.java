package com.example.api.external.dto.langchain;

import com.example.api.entity.ParsedText;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureEmbeddingRequest {
    @JsonProperty("parsed_text")
    private ParsedText parsedText;
}

