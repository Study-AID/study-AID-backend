package com.example.api.external.dto.langchain;

import com.example.api.adapters.llm.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageContextRequest {
    @JsonProperty("chat_id")
    private UUID chatId;
    private List<ChatMessage> messages;
}
