package com.example.api.external.dto.langchain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageHistoryRequest {
    private UUID chatId;
    private String question;
    private String answer;
}
