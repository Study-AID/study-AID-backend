package com.example.api.adapters.sqs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCourseWeaknessAnalysisMessage {
    private String schemaVersion;
    private UUID requestId;
    private OffsetDateTime occurredAt;
    private UUID userId;
    private UUID quizId;    // 퀴즈인 경우 (null 가능)
    private UUID examId;    // 모의시험인 경우 (null 가능)
    private UUID courseId;
}