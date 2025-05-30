package com.example.api.entity;

import com.example.api.entity.enums.MessageRole;
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
        name = "qna_chat_messages",
        schema = "app",
        indexes = {
                @Index(name = "idx_qna_chat_messages_qna_chat_created_at", columnList = "qna_chat_id, created_at")
        }
)
public class QnaChatMessage {
    @Id
    @Column()
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qna_chat_id", nullable = false)
    private QnaChat qnaChat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    @Column(name = "content")
    private String content;

    @Column(name = "is_liked", nullable = false)
    private Boolean isLiked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (isLiked == null) {
            isLiked = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}