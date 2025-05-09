package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.exception.InternalServerErrorException;
import com.example.api.exception.NotFoundException;
import com.example.api.external.dto.langchain.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LangchainClientImpl implements LangchainClient {

    private static final Logger log = LoggerFactory.getLogger(LangchainClientImpl.class);

    private final RestTemplate restTemplate;

    @Value("${langchain.server.url}")
    private String langchainServerUrl;

    @Override
    public VectorizeLectureResponse vectorizeLecture(UUID lectureId, String parsedText) {
        log.info("[LangchainClient] 호출: vectorizeLecture(lectureId={})", lectureId);

        VectorizeLectureRequest body = new VectorizeLectureRequest(lectureId, parsedText);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VectorizeLectureRequest> request = new HttpEntity<>(body, headers);

        try {ResponseEntity<VectorizeLectureResponse> response = restTemplate.exchange(
                    langchainServerUrl + "/vectors",
                    HttpMethod.POST,
                    request,
                    VectorizeLectureResponse.class
            );

            if (response.getBody() == null) {
                throw new InternalServerErrorException("Langchain 벡터화 응답이 비었습니다.");
            }

            log.info("[LangchainClient] 벡터화 완료: {} chunks 생성됨", response.getBody().getTotalChunks());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Langchain /vectors 요청 실패: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain 벡터화 요청 실패: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Langchain 벡터화 중 알 수 없는 예외 발생: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain 벡터화 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public ReferenceResponse findReferences(UUID lectureId, String question, int topK) {
        try {log.info("[LangchainClient] 호출: findReferences(lectureId={}, question={}, topK={})", lectureId, question, topK);

            ReferenceRequest requestDto = new ReferenceRequest(lectureId, question, topK);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ReferenceRequest> request = new HttpEntity<>(requestDto, headers);

            ResponseEntity<ReferenceResponse> response = restTemplate.exchange(
                    langchainServerUrl + "/references",
                    HttpMethod.POST,
                    request,
                    ReferenceResponse.class
            );

            log.info("[LangchainClient] 성공: 출처 {}개 반환됨", response.getBody().getReferences().size());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Langchain 서버에서 lecture index 없음 (404)");
                throw new NotFoundException("강의자료 벡터 인덱스가 존재하지 않습니다.");
            }
            log.error("Langchain 4xx 응답: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain 서버에서 4xx 에러가 발생했습니다.");
        } catch (Exception e) {
            log.error("Langchain references API 실패: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain 서버 호출 중 에러 발생");
        }
    }

    @Override
    public MessageContextResponse appendMessages(UUID chatId, List<ChatMessage> messages) {
        log.info("[LangchainClient] 호출: appendMessages(chatId={}, messageCount={})", chatId, messages.size());

        MessageContextRequest requestDto = new MessageContextRequest(chatId, messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MessageContextRequest> request = new HttpEntity<>(requestDto, headers);

        try {ResponseEntity<MessageContextResponse> response = restTemplate.exchange(
                    langchainServerUrl + "/messages",
                    HttpMethod.POST,
                    request,
                    MessageContextResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<ChatMessage> messageContext = response.getBody().getLangchainChatContext();
                log.info("[LangchainClient] appendMessages 성공: message count = {}", messageContext != null ? messageContext.size() : 0);
                return new MessageContextResponse(messageContext != null ? messageContext : List.of());
            } else {
                log.warn("[LangchainClient] appendMessages 응답 없음 또는 바디 null");
                return new MessageContextResponse(List.of());
            }
        } catch (HttpClientErrorException e) {
            log.error("Langchain appendMessages 4xx 응답: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain appendMessages 요청 실패: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Langchain appendMessages 호출 실패: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain appendMessages 중 서버 오류가 발생했습니다.");
        }
    }


    @Override
    public MessageContextResponse getMessageContext(UUID chatId) {
        log.info("[LangchainClient] 호출: getMessageContext(chatId={})", chatId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {ResponseEntity<MessageContextResponse> response = restTemplate.exchange(
                    langchainServerUrl + "/messages-context?chat_id=" + chatId,
                    HttpMethod.GET,
                    request,
                    MessageContextResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<ChatMessage> messageContext = response.getBody().getLangchainChatContext();
                log.info("[LangchainClient] getMessageContext 응답 수신 완료: message count = {}", messageContext != null ? messageContext.size() : 0);
                return new MessageContextResponse(messageContext != null ? messageContext : List.of());
            } else {
                log.warn("Langchain 서버에서 응답 없음 또는 바디 null");
                return new MessageContextResponse(List.of());
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Langchain 서버에 해당 chat_id={} 없습니다. 새 대화 시작으로 간주합니다.", chatId);
            return new MessageContextResponse(List.of());
        } catch (Exception e) {
            log.error("Langchain getMessageContext 호출 실패: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Langchain 서버 호출 중 에러 발생");
        }
    }
}
