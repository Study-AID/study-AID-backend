package com.example.api.config;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAIConfig {
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    /**
     * OpenAiApi에 커스텀 WebClient 주입 (타임아웃 포함)
     */
    @Bean
    public OpenAiApi openAiApi(WebClient.Builder webClientBuilder) {
        return new OpenAiApi(apiKey);
    }
    /**
     * Spring AI의 GPT 채팅 클라이언트 생성
     */
    @Bean
    public OpenAiChatClient openAiChatClient(OpenAiApi openAiApi) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel(model)
                .build();
        return new OpenAiChatClient(openAiApi, chatOptions);
    }
}
