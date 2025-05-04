package com.example.api.controller.dto.qna;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateQnaChatRequest {
    private UUID lectureId;
}

