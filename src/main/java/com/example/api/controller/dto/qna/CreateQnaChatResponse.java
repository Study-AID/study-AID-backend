package com.example.api.controller.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateQnaChatResponse {
    private UUID chatId;
}
