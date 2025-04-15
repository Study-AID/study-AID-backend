package com.example.api.adapters.llm;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAIAdapter implements LLMAdapter {
    private final OpenAiChatClient openAiChatClient;

    private static final String PROVIDER_NAME = "OpenAI";

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
}
