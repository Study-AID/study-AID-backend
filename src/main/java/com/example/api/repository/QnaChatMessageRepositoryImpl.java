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
    public Page<QnaChatMessage> findByQnaChatIdWithPagination(UUID chatId, Pageable pageable) {
        TypedQuery<QnaChatMessage> query = manager.createQuery(
                        "SELECT m FROM QnaChatMessage m WHERE m.qnaChat.id = :chatId ORDER BY m.createdAt ASC",
                        QnaChatMessage.class)
                .setParameter("chatId", chatId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<QnaChatMessage> messages = query.getResultList();

        Long totalCount = manager.createQuery(
                        "SELECT COUNT(m) FROM QnaChatMessage m WHERE m.qnaChat.id = :chatId",
                        Long.class)
                .setParameter("chatId", chatId)
                .getSingleResult();

        return new PageImpl<>(messages, pageable, totalCount);
    }
}
