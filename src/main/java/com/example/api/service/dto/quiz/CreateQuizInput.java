package com.example.api.service.dto.quiz;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateQuizInput {
    private UUID lectureId;
    private UUID userId;
    private String title;
    private String parsedText;
    private int trueOrFalseCount;
    private int multipleChoiceCount;
    private int shortAnswerCount;
    private int essayCount;
}
