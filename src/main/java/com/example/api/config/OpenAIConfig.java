package com.example.api.config;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Bean
    public OpenAiChatClient openAiChatClient(OpenAiApi openAiApi) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel(model)
                .build();
        return new OpenAiChatClient(openAiApi, chatOptions);
    }

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(apiKey);

    }
}
