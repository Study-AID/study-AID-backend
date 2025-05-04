package com.example.api.external.dto.langchain;

import com.example.api.adapters.llm.ChatMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class MessageHistoryResponse {
    private List<ChatMessage> messageHistory;
}
