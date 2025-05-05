package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.MessageHistoryResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;

import java.util.List;
import java.util.UUID;

public interface LangchainClient {
    ReferenceResponse findReferences(UUID lectureId, String question, int topK);
    MessageHistoryResponse appendMessage(UUID chatId, String question, String answer);
    MessageHistoryResponse getMessageHistory(UUID chatId);
}
