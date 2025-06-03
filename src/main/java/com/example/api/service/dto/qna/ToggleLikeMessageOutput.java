
package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleLikeMessageOutput {
    private boolean isLiked;
    private String action;
}