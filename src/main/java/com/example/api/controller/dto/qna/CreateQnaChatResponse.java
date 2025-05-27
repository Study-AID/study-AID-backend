package com.example.api.controller.dto.qna;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateQnaChatResponse {
    @NotNull
    private UUID chatId;
    @NotNull
    private LocalDateTime createdAt;
}
