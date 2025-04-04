package com.example.api.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Check;

import com.example.api.entity.enums.QuestionType;
import com.example.api.entity.enums.Status;

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


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "exam_items",
    schema = "app",
    indexes = {
        @Index(name = "idx_exam_items_exam_display_order", columnList = "exam_id, display_order")
    }
)
@Check(constraints = "question_type IN ('true_or_false', 'multiple_choice', 'short_answer', 'essay', 'custom')")
public class ExamItem {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "question", nullable = false, columnDefinition = "text")
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    @Column(name = "is_true_answer")
    private Boolean isTrueAnswer;

    @Column(name = "choices", columnDefinition = "text[]")
    private String[] choices;

    @Column(name = "answer_indices", columnDefinition = "int[]")
    private Integer[] answerIndices;

    @Column(name = "text_answer", columnDefinition = "text")
    private String textAnswer;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "points")
    private Float points;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
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
            points = (float) 10;
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
