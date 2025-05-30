package com.example.api.service.dto.lecture;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateLectureInput {
    private UUID courseId;
    private UUID userId;
    private String title;
    private String materialPath;
    private String materialType;
    private String displayOrderLex;
}
