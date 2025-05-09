package com.example.api.adapters.llm;

import com.example.api.promptsupport.PromptLoader;
import com.example.api.promptsupport.PromptTemplate;
import com.example.api.promptsupport.PromptPaths;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class OpenAIAdapter implements LLMAdapter {
    private static final Logger log = LoggerFactory.getLogger(OpenAIAdapter.class);

    private static final String PROVIDER_NAME = "OpenAI";
    private final OpenAiChatClient openAiChatClient;

    public OpenAIAdapter(OpenAiChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    @Override
    public String complete(String prompt) {
        Message message = new UserMessage(prompt);
        Prompt openAiPrompt = new Prompt(message);

        // OpenAI API 호출 및 결과 반환
        return openAiChatClient.call(openAiPrompt).
                getResult().
                getOutput().
                getContent();
    }

    @Override
    public String chat(ChatMessage[] messages) {
        List<Message> messageList = convertToSpringMessages(messages);

        // Prompt 생성 및 API 호출
        Prompt openAiPrompt = new Prompt(messageList);
        return openAiChatClient.call(openAiPrompt).
                getResult().
                getOutput().
                getContent();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * 내부 ChatMessage 객체를 Spring AI Message 객체로 변환.
     */
    private List<Message> convertToSpringMessages(ChatMessage[] messages) {
        List<Message> springMessages = new ArrayList<>();

        for (ChatMessage message : messages) {
            String content = message.getContent();
            switch (message.getRole().toLowerCase()) {
                case "system":
                    springMessages.add(new SystemMessage(content));
                    break;
                case "user":
                    springMessages.add(new UserMessage(content));
                    break;
                case "assistant":
                    springMessages.add(new AssistantMessage(content));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unsupported role:" + message.getRole()
                    );
            }
        }
        return springMessages;
    }

    @Override
    public String ask(String question, List<String> referenceChunks, List<ChatMessage> messageHistory) {
        try {
            log.info("[OpenAIAdapter] GPT 요청 준비 시작");
            PromptTemplate promptTemplate = PromptLoader.load(PromptPaths.QNA_ANSWER_V1);

            String chunkBlock = String.join("\n\n", referenceChunks);
            log.debug("[OpenAIAdapter] 출처 청크 블록 생성 완료: {} 자", chunkBlock.length());
            String systemContent = promptTemplate.getSystem().replace("{{chunks}}", chunkBlock);
            String userContent = promptTemplate.getUser().replace("{{question}}", question);

            List<ChatMessage> allMessages = new ArrayList<>();
            // 출처
            allMessages.add(new ChatMessage("system", systemContent));
            // 이전 대화 맥락
            allMessages.addAll(messageHistory);
            // 사용자 질문
            allMessages.add(new ChatMessage("user", userContent));

            ChatMessage[] allMessagesArray = allMessages.toArray(new ChatMessage[0]);

            // 대략적인 토큰 수 추정
            int totalTokenEstimate = Arrays.stream(allMessagesArray)
                    .mapToInt(m -> m.getContent() != null ? m.getContent().length() : 0)
                    .sum() / 4;

            log.info("[OpenAIAdapter] GPT 요청 구성 완료: 전체 메시지 수 = {}, 예상 토큰 수 = {}",
                    allMessagesArray.length, totalTokenEstimate);
            log.info("[OpenAIAdapter] GPT 호출 직전: 전체 prompt 길이 (글자 수) = {}",
                    Arrays.stream(allMessagesArray).mapToInt(m -> m.getContent().length()).sum());

            // 각 메시지 로깅 (처음 100자만)
            for (int i = 0; i < allMessagesArray.length; i++) {
                ChatMessage m = allMessagesArray[i];
                String content = m.getContent();
                log.debug("[OpenAIAdapter] 메시지 #{} [{}]: {}",
                        i, m.getRole(),
                        content != null ? content.substring(0, Math.min(content.length(), 100)) : "null");
            }

            log.info("[OpenAIAdapter] OpenAI API 호출 시작...");
            long startTime = System.currentTimeMillis();

            String result = chat(allMessagesArray);

            long endTime = System.currentTimeMillis();
            log.info("[OpenAIAdapter] OpenAI API 응답 수신 완료: 소요 시간 {}ms, 응답 길이 {}자",
                    (endTime - startTime), result.length());
            log.debug("[OpenAIAdapter] GPT 응답 내용 일부: {}",
                    result.substring(0, Math.min(result.length(), 200)));

            return result;
        } catch (Exception e) {
            log.error("[OpenAIAdapter] GPT 호출 중 예외 발생: {}", e.getMessage());

            // 예외 유형별 상세 로깅
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                org.springframework.web.reactive.function.client.WebClientResponseException webEx =
                        (org.springframework.web.reactive.function.client.WebClientResponseException) e;
                log.error("[OpenAIAdapter] HTTP 상태 코드: {}", webEx.getStatusCode());
                log.error("[OpenAIAdapter] 응답 본문: {}", webEx.getResponseBodyAsString());
            } else if (e instanceof java.util.concurrent.TimeoutException) {
                log.error("[OpenAIAdapter] 요청 타임아웃 발생");
            } else if (e instanceof java.net.ConnectException) {
                log.error("[OpenAIAdapter] 연결 실패: {}", e.getMessage());
            }

            log.error("[OpenAIAdapter] 스택 트레이스:", e);

            throw new RuntimeException("OpenAI API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
