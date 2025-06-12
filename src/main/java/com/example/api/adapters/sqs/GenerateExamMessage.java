package com.example.api.adapters.sqs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateExamMessage {
    private String schemaVersion;
    private UUID requestId;
    private OffsetDateTime occurredAt;
    private UUID userId;
    private UUID courseId;
    private UUID examId;
    private String title;
    @JsonProperty("referenced_lecture_ids")
    private UUID[] referencedLectures;
    private int trueOrFalseCount;
    private int multipleChoiceCount;
    private int shortAnswerCount;
    private int essayCount;
}
