package com.example.api.external.dto.langchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VectorizeLectureRequest {
    @JsonProperty("lecture_id")
    private UUID lectureId;

    @JsonProperty("parsed_text")
    private String parsedText;
}

