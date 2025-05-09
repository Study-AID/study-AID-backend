package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.external.dto.langchain.MessageContextResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;
import com.example.api.external.dto.langchain.VectorizeLectureResponse;

import java.util.List;
import java.util.UUID;

public interface LangchainClient {
    VectorizeLectureResponse vectorizeLecture(UUID lectureId, String parsedText);
    ReferenceResponse findReferences(UUID lectureId, String question, int topK);
    MessageContextResponse appendMessages(UUID chatId, List<ChatMessage> messages);
    MessageContextResponse getMessageContext(UUID chatId);
}
