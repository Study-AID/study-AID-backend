// LikedQnaAnswerRepository.java - 수정된 버전
package com.example.api.repository;

import com.example.api.entity.LikedQnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LikedQnaAnswerRepository extends JpaRepository<LikedQnaAnswer, UUID> {
    // 좋아요 존재 여부 확인
    boolean existsByQnaChatIdAndQnaChatMessageIdAndUserId(UUID chatId, UUID messageId, UUID userId);
    // 사용자가 좋아요한 메시지들 조회
    List<LikedQnaAnswer> findByQnaChatIdAndUserId(UUID chatId, UUID userId);
    // 좋아요 삭제
    void deleteByQnaChatIdAndQnaChatMessageIdAndUserId(UUID chatId, UUID messageId, UUID userId);
}