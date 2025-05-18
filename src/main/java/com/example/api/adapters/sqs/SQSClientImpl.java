package com.example.api.adapters.sqs;

import com.example.api.config.SQSMessageConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Component
@RequiredArgsConstructor
public class SQSClientImpl implements SQSClient {
    private static final Logger logger = LoggerFactory.getLogger(SQSClientImpl.class);

    private final SqsClient sqsClient;
    private final SQSMessageConfig sqsMessageConfig;

    @Override
    public void sendGenerateSummaryMessage(GenerateSummaryMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            String messageBody = mapper.writeValueAsString(message);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(sqsMessageConfig.getGenerateSummary().getQueueUrl())
                    .messageBody(messageBody)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            logger.info("Successfully sent generate summary message with requestId: {} and messageId: {}",
                    message.getRequestId(), response.messageId());

        } catch (Exception e) {
            logger.error("Failed to send generate summary message for lectureId: {}", message.getLectureId(), e);
            throw new RuntimeException("Failed to send SQS message", e);
        }
    }

    @Override
    public void sendGenerateQuizMessage(GenerateQuizMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            String messageBody = mapper.writeValueAsString(message);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(sqsMessageConfig.getGenerateQuiz().getQueueUrl())
                    .messageBody(messageBody)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            logger.info("Successfully sent generate quiz message with requestId: {} and messageId: {}",
                    message.getRequestId(), response.messageId());

        } catch (Exception e) {
            logger.error("Failed to send generate quiz message for lectureId: {}", message.getLectureId(), e);
            throw new RuntimeException("Failed to send SQS message", e);
        }
    }
}
