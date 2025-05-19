package com.example.api.service.dto.exam;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateExamInput {
    private UUID courseId;
    private UUID userId;
    private String title;
    private UUID[] referencedLectures;
}
