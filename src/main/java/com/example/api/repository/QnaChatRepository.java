package com.example.api.repository;

import com.example.api.entity.QnaChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QnaChatRepository extends JpaRepository<QnaChat, UUID> {
    // 강의 ID와 사용자 ID로 QnaChat 찾기
    Optional<QnaChat> findByLectureIdAndUserId(UUID lectureId, UUID userId);
}