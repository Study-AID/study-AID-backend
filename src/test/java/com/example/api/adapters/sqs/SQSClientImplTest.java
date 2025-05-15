package com.example.api.adapters.sqs;

import com.example.api.config.SQSMessageConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SQSClientImpl 테스트")
class SQSClientImplTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private SQSMessageConfig sqsMessageConfig;

    private SQSClientImpl sqsClientImpl;

    @BeforeEach
    void setUp() {
        SQSMessageConfig.GenerateSummary generateSummary = new SQSMessageConfig.GenerateSummary();
        generateSummary.setQueueUrl("http://localhost:4566/000000000000/test-queue");
        when(sqsMessageConfig.getGenerateSummary()).thenReturn(generateSummary);

        sqsClientImpl = new SQSClientImpl(sqsClient, sqsMessageConfig);
    }

    @Test
    @DisplayName("GenerateSummaryMessage 전송 성공")
    void sendGenerateSummaryMessage_Success() throws Exception {
        // Given
        GenerateSummaryMessage message = GenerateSummaryMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(UUID.randomUUID())
                .courseId(UUID.randomUUID())
                .lectureId(UUID.randomUUID())
                .s3Bucket("test-bucket")
                .s3Key("test-key.pdf")
                .build();

        SendMessageResponse mockResponse = SendMessageResponse.builder()
                .messageId("test-message-id")
                .build();

        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(mockResponse);

        // When
        assertDoesNotThrow(() -> sqsClientImpl.sendGenerateSummaryMessage(message));

        // Then
        ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient, times(1)).sendMessage(requestCaptor.capture());

        SendMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals("http://localhost:4566/000000000000/test-queue", capturedRequest.queueUrl());
        assertNotNull(capturedRequest.messageBody());
    }

    @Test
    @DisplayName("GenerateSummaryMessage 전송 실패")
    void sendGenerateSummaryMessage_Failure() throws Exception {
        // Given
        GenerateSummaryMessage message = GenerateSummaryMessage.builder()
                .schemaVersion("1.0.0")
                .requestId(UUID.randomUUID())
                .occurredAt(OffsetDateTime.now())
                .userId(UUID.randomUUID())
                .courseId(UUID.randomUUID())
                .lectureId(UUID.randomUUID())
                .s3Bucket("test-bucket")
                .s3Key("test-key.pdf")
                .build();

        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> sqsClientImpl.sendGenerateSummaryMessage(message));

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }
}
