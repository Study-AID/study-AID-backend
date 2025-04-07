package com.example.api.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import com.example.api.entity.enums.QuestionType;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "quiz_items", 
    schema = "app",
    indexes = {
        @Index(name = "idx_quiz_items_quiz_display_order", columnList = "quiz_id, display_order")
    }
)
@Check(constraints = "question_type IN ('true_or_false', 'multiple_choice', 'short_answer', 'essay', 'custom')")
public class QuizItem {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(name = "is_true_answer")
    private Boolean isTrueAnswer;

    @Column(columnDefinition = "text[]")
    private String[] choices;

    @Column(name = "answer_indices", columnDefinition = "int[]")
    private Integer[] answerIndices;

    @Column(name = "text_answer", columnDefinition = "text")
    private String textAnswer;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column
    private Float points;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (points == null) {
            points = 10.0f;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}