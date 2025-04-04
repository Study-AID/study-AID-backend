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

import com.example.api.entity.enums.SummaryStatus;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "lectures", 
    schema = "app",
    indexes = {
        @Index(name = "idx_lectures_course_created_at", columnList = "course_id, created_at"),
        @Index(name = "idx_lectures_course_updated_at", columnList = "course_id, updated_at"),
        @Index(name = "idx_lectures_course_display_order_lex", columnList = "course_id, display_order_lex")
    }
)
@Check(constraints = " IN ('not_started', 'in_progress', 'completed')")
public class Lecture {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "material_path", nullable = false, length = 255)
    private String materialPath;

    @Column(name = "material_type", nullable = false, length = 20)
    private String materialType;

    @Column(name = "display_order_lex", nullable = false, length = 255)
    private String displayOrderLex;

    @Column(columnDefinition = "jsonb")
    private String note;

    @Column(columnDefinition = "jsonb")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_status", nullable = false, length = 20)
    private SummaryStatus summaryStatus;

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
        if (summaryStatus == null) {
            summaryStatus = SummaryStatus.not_started;
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