package com.example.api.adapters.sqs;

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
public class GenerateSummaryMessage {
    private String schemaVersion;
    private UUID requestId;
    private OffsetDateTime occurredAt;
    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID courseId;
    private UUID lectureId;
    private String lectureTitle;
    private String s3Bucket;
    private String s3Key;
}
