package com.example.api.controller.dto.qna;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleLikeMessageResponse {
    @NotNull
    @JsonProperty("isLiked")
    private boolean isLiked;
    @NotNull
    private String action;
}