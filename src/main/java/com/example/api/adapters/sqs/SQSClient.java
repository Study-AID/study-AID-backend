package com.example.api.adapters.sqs;

public interface SQSClient {
    void sendGenerateSummaryMessage(GenerateSummaryMessage message);
    
    void sendGenerateQuizMessage(GenerateQuizMessage message);

    void sendGenerateExamMessage(GenerateExamMessage message);

    void sendGradeQuizEssayMessage(GradeQuizEssayMessage message);
}
