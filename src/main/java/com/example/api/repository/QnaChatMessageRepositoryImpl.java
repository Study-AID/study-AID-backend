package com.example.api.repository;

import com.example.api.entity.QnaChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public class QnaChatMessageRepositoryImpl implements QnaChatMessageRepositoryCustom {
    @PersistenceContext
    private EntityManager manager;

    @Override
    public List<QnaChatMessage> findByQnaChatIdWithCursor(UUID chatId, UUID cursor, int limit) {
        String jpql;
        TypedQuery<QnaChatMessage> query;

        if (cursor == null) {
            // 첫 로드: 최신 메시지부터
            jpql = "SELECT m FROM QnaChatMessage m WHERE m.qnaChat.id = :chatId " +
                    "ORDER BY m.createdAt DESC";
            query = manager.createQuery(jpql, QnaChatMessage.class)
                    .setParameter("chatId", chatId);
        } else {
            // 이전 메시지들: cursor보다 오래된 메시지들
            jpql = "SELECT m FROM QnaChatMessage m WHERE m.qnaChat.id = :chatId " +
                    "AND m.createdAt < (SELECT cm.createdAt FROM QnaChatMessage cm WHERE cm.id = :cursor) " +
                    "ORDER BY m.createdAt DESC";
            query = manager.createQuery(jpql, QnaChatMessage.class)
                    .setParameter("chatId", chatId)
                    .setParameter("cursor", cursor);
        }

        return query.setMaxResults(limit + 1) // +1로 hasMore 판단
                .getResultList();
    }
}
