package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.MessageHistoryRequest;
import com.example.api.external.dto.langchain.MessageHistoryResponse;
import com.example.api.external.dto.langchain.ReferenceRequest;
import com.example.api.external.dto.langchain.ReferenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LangchainClientImpl implements LangchainClient {

    private final RestTemplate restTemplate;

    @Value("${langchain.server.url}")
    private String langchainServerUrl;

    @Override
    public List<String> findReferences(UUID lectureId, String question, int topK) {
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
            throw new RuntimeException("Langchain 서버에서 출처를 가져오지 못했습니다.");
        }

        List<String> chunks = new ArrayList<>();
        response.getBody().getReferences().forEach(c -> chunks.add(c.getText()));
        return chunks;
    }

    @Override
    public List<ChatMessage> generateMessageHistory(UUID chatId, UUID lectureId, String question, List<MessageHistoryRequest.MessageHistoryItem> history) {
        MessageHistoryRequest requestDto = new MessageHistoryRequest(chatId, lectureId, question, history);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MessageHistoryRequest> request = new HttpEntity<>(requestDto, headers);

        ResponseEntity<MessageHistoryResponse> response = restTemplate.exchange(
                langchainServerUrl + "/history",
                HttpMethod.POST,
                request,
                MessageHistoryResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null || response.getBody().getMessageHistory() == null) {
            throw new RuntimeException("Langchain 서버에서 채팅 메세지 history를 가져오지 못했습니다.");
        }

        return response.getBody().getMessageHistory();
    }
}
