// LikedQnaAnswerRepository.java - 수정된 버전
package com.example.api.repository;

import com.example.api.entity.LikedQnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LikedQnaAnswerRepository extends JpaRepository<LikedQnaAnswer, UUID>, LikedQnaAnswerRepositoryCustom {
    // 좋아요 존재 여부 확인
    boolean existsByQnaChatIdAndQnaChatMessageIdAndUserId(UUID chatId, UUID messageId, UUID userId);
}