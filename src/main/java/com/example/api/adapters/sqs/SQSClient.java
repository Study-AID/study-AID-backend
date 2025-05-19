package com.example.api.adapters.sqs;

public interface SQSClient {
    void sendGenerateSummaryMessage(GenerateSummaryMessage message);
}
