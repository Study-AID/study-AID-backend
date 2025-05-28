package com.example.api.controller.dto.qna;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleLikeMessageResponse {
    @NotNull
    private boolean liked;
    @NotNull
    private String action;
}