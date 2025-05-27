package com.example.api.service.dto.exam;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ToggleLikeExamItemOutput {
    private UUID examId;
    private UUID examItemId;
    private UUID userId;
    private boolean isLiked;

    public ToggleLikeExamItemOutput(UUID examId, UUID examItemId, UUID userId, boolean isLiked) {
        this.examId = examId;
        this.examItemId = examItemId;
        this.userId = userId;
        this.isLiked = isLiked;
    }
}
