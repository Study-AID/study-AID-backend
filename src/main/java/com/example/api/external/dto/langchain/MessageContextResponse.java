package com.example.api.external.dto.langchain;

import com.example.api.adapters.llm.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageContextResponse {
    @JsonProperty("buffer_window_context")
    private List<ChatMessage> langchainChatContext;
}
