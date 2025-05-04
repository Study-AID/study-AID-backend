package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.MessageHistoryRequest;

import java.util.List;
import java.util.UUID;

public interface LangchainClient {
    List<String> findReferences(UUID lectureId, String question, int topK);
    List<ChatMessage> generateMessageHistory(UUID chatId, UUID lectureId, String question, List<MessageHistoryRequest.MessageHistoryItem> history);
}
