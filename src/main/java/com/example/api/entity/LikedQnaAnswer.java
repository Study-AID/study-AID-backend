package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "liked_qna_answers",
        schema = "app",
        indexes = {
                @Index(name = "idx_liked_qna_answers_chat_user", columnList = "qna_chat_id, user_id"),
                @Index(name = "idx_liked_qna_answers_chat_msg_user", columnList = "qna_chat_id, message_id, user_id"),
                @Index(name = "idx_liked_qna_answers_qna_chat_created_at", columnList = "qna_chat_id, created_at")
        }
)
public class LikedQnaAnswer {
    @Id
    @Column()
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_chat_id", nullable = false)
    private QnaChat qnaChat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private QnaChatMessage qnaChatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}