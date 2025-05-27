package com.example.api.service.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetLikedMessagesInput {
    private UUID lectureId;
    private UUID userId;
}
