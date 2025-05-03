package com.example.api.external;

import com.example.api.external.dto.rag.RagAnswer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
@Component
public class RagClientImpl implements RagClient {

    private final RestTemplate restTemplate;

    @Value("${rag.server.url}")
    private String ragServerUrl;

    @Override
    public RagAnswer query(String question, String parsedText) {
        // JSON body 구성
        String requestBody = String.format("{\"question\":\"%s\", \"context\":\"%s\"}",
                escapeJson(question), escapeJson(parsedText));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // 요청 전송 및 응답 받기
        ResponseEntity<RagAnswer> response = restTemplate.postForEntity(
                ragServerUrl + "/rag",
                request,
                RagAnswer.class
        );

        return response.getBody();
    }

    // JSON 이스케이프 유틸
    private String escapeJson(String input) {
        return input.replace("\"", "\\\""); // 큰따옴표 이스케이프
    }
}
