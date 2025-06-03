package com.example.api.repository;

import com.example.api.entity.QnaChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface QnaChatMessageRepositoryCustom {
    List<QnaChatMessage> findByQnaChatIdWithCursor(UUID chatId, UUID cursor, int limit);
}
