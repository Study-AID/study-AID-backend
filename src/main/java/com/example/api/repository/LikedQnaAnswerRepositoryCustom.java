package com.example.api.repository;

import com.example.api.entity.LikedQnaAnswer;
import java.util.List;
import java.util.UUID;

public interface LikedQnaAnswerRepositoryCustom {
    // 좋아요 삭제
    void deleteByQnaChatIdAndQnaChatMessageIdAndUserId(UUID chatId, UUID messageId, UUID userId);
    // 특정 채팅방의 좋아요 메세지 목록 조회
    List<LikedQnaAnswer> findByQnaChatIdAndUserIdWithMessage(UUID chatId, UUID userId);
}