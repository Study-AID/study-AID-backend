package com.example.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sqs")
@Getter
@Setter
public class SQSMessageConfig {
    private GenerateSummary generateSummary = new GenerateSummary();

    @Getter
    @Setter
    public static class GenerateSummary {
        private String queueUrl;
    }
}
