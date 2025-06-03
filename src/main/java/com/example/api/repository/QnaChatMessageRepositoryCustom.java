package com.example.api.repository;

import com.example.api.entity.QnaChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface QnaChatMessageRepositoryCustom {
    Page<QnaChatMessage> findByQnaChatIdWithPagination(UUID chatId, Pageable pageable);
}
