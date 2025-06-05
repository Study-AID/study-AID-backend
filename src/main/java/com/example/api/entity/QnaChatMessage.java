package com.example.api.entity;

import com.example.api.entity.enums.MessageRole;
import com.example.api.external.dto.langchain.ReferenceResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reference_chunks", columnDefinition = "jsonb") // references가 postgres 예약어라서 db에서는 reference_chunks로 저장
    private List<ReferenceResponse.ReferenceChunkResponse> references;

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