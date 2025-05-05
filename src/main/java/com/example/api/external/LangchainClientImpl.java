package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.exception.BadRequestException;
import com.example.api.external.dto.langchain.MessageHistoryRequest;
import com.example.api.external.dto.langchain.MessageHistoryResponse;
import com.example.api.external.dto.langchain.ReferenceRequest;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LangchainClientImpl implements LangchainClient {

    private final RestTemplate restTemplate;

    @Value("${langchain.server.url}")
    private String langchainServerUrl;

    @Override
    public ReferenceResponse findReferences(UUID lectureId, String question, int topK) {
        ReferenceRequest requestDto = new ReferenceRequest(question, topK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReferenceRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<ReferenceResponse> response = restTemplate.exchange(
                langchainServerUrl + "/reference",
                HttpMethod.POST,
                request,
                ReferenceResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null || response.getBody().getReferences() == null) {
            throw new BadRequestException("Langchain 서버에서 출처를 가져오지 못했습니다.");
        }

        return response.getBody();
    }

    @Override
    public MessageHistoryResponse appendMessage(UUID chatId, String question, String answer) {
        MessageHistoryRequest requestDto = new MessageHistoryRequest(chatId, question, answer);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MessageHistoryRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<MessageHistoryResponse> response = restTemplate.exchange(
                langchainServerUrl + "/messages",
                HttpMethod.POST,
                request,
                MessageHistoryResponse.class
        );
        return response.getBody();
    }

    @Override
    public MessageHistoryResponse getMessageHistory(UUID chatId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<MessageHistoryResponse> response = restTemplate.exchange(
                langchainServerUrl + "/messages-history?chat_id=" + chatId,
                HttpMethod.GET,
                request,
                MessageHistoryResponse.class
        );
        return response.getBody();
    }
}