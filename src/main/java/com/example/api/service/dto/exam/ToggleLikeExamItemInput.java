package com.example.api.service.dto.exam;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ToggleLikeExamItemInput {
    private UUID examId;
    private UUID examItemId;
    private UUID userId;
}
