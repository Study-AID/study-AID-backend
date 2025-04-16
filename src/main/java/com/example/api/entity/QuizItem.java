package com.example.api.entity;

import com.example.api.entity.enums.QuestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Column()
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column()
    private String explanation;

    @Column(name = "is_true_answer")
    private Boolean isTrueAnswer;

    @Column()
    private String[] choices;

    @Column(name = "answer_indices")
    private Integer[] answerIndices;

    @Column(name = "text_answer")
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