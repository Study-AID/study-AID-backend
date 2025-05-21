package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class CreateQnaChatOutput {
    private UUID chatId;
}
