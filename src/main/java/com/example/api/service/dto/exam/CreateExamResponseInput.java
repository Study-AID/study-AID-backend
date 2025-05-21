package com.example.api.service.dto.exam;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateExamResponseInput {
    private UUID examId;

    private UUID examItemId;

    private UUID userId;

    private Boolean selectedBool;

    private Integer[] selectedIndices;

    private String textAnswer;
}
