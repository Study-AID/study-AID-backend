package com.example.api.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;

// 현재 GPT API 호출 커스터마이징을 위해 Spring AI의 OpenAiChatClient 대신 OpenAiAdapter + WebClient를 사용하고 있습니다.
@Configuration
public class WebClientConfig {
    @Value("${web-client.connect-timeout-ms:3000}")
    private int connectTimeoutMs; // 3초

    @Value("${web-client.response-timeout-ms:60000}")
    private int responseTimeoutMs; // 60초

    @Value("${web-client.max-in-memory-size:10485760}")
    private int maxInMemorySize; // 10MB

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs));

        return WebClient.builder()
                .baseUrl("https://api.openai.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(maxInMemorySize))
                        .build()
                )
                .defaultHeader("Content-Type", "application/json");
    }
}
