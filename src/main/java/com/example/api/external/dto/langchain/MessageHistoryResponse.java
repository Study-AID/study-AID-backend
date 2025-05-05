package com.example.api.external.dto.langchain;

import com.example.api.adapters.llm.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MessageHistoryResponse {
    private List<ChatMessage> langchainChatHistory;
}
