package com.example.api.external;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.entity.ParsedText;
import com.example.api.external.dto.langchain.EmbeddingCheckResponse;
import com.example.api.external.dto.langchain.MessageContextResponse;
import com.example.api.external.dto.langchain.LectureEmbeddingResponse;
import com.example.api.external.dto.langchain.ReferenceResponse;

import java.util.List;
import java.util.UUID;

public interface LangchainClient {
    LectureEmbeddingResponse generateLectureEmbeddings(UUID lectureId, ParsedText parsedText);
    EmbeddingCheckResponse checkEmbeddingStatus(UUID lectureId);
    ReferenceResponse findReferencesInLecture(UUID lectureId, String question, int maxNumReferences, double minSimilarity);
    MessageContextResponse appendMessages(UUID chatId, List<ChatMessage> messages);
    MessageContextResponse getMessageContext(UUID chatId);
}
