package com.example.api.external.dto.langchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReferenceRequest {
    @JsonProperty("lecture_id")
    private UUID lectureId;

    private String question;

    private int k;
}