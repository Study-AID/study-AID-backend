package com.example.api.repository;

import com.example.api.entity.LikedQnaAnswer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class LikedQnaAnswerRepositoryImpl implements LikedQnaAnswerRepositoryCustom {
    private static final Logger logger = LoggerFactory.getLogger(LikedQnaAnswerRepositoryImpl.class);

    @PersistenceContext
    private EntityManager manager;

    @Override
    @Transactional
    public void deleteByQnaChatIdAndQnaChatMessageIdAndUserId(UUID chatId, UUID messageId, UUID userId) {
        try {
            int deletedCount = manager.createQuery(
                            "DELETE FROM LikedQnaAnswer l " +
                                    "WHERE l.qnaChat.id = :chatId " +
                                    "AND l.qnaChatMessage.id = :messageId " +
                                    "AND l.user.id = :userId")
                    .setParameter("chatId", chatId)
                    .setParameter("messageId", messageId)
                    .setParameter("userId", userId)
                    .executeUpdate();

            logger.info("좋아요 삭제 완료: chatId={}, messageId={}, userId={}, deletedCount={}",
                    chatId, messageId, userId, deletedCount);
        } catch (Exception e) {
            logger.error("좋아요 삭제 실패: chatId={}, messageId={}, userId={}",
                    chatId, messageId, userId, e);
            throw new RuntimeException("좋아요 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<LikedQnaAnswer> findByQnaChatIdAndUserIdWithMessage(UUID chatId, UUID userId) {
        return manager.createQuery(
                        "SELECT l FROM LikedQnaAnswer l " +
                                "JOIN FETCH l.qnaChatMessage " +
                                "WHERE l.qnaChat.id = :chatId " +
                                "AND l.user.id = :userId " +
                                "ORDER BY l.createdAt DESC",
                        LikedQnaAnswer.class)
                .setParameter("chatId", chatId)
                .setParameter("userId", userId)
                .getResultList();
    }
}