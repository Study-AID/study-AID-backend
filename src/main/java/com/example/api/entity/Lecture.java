package com.example.api.entity;

import com.example.api.entity.enums.SummaryStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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
@Check(constraints = "summary_status IN ('not_started', 'in_progress', 'completed')")
public class Lecture {
    @Id
    @Column()
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

    @Column(name = "parsed_text", columnDefinition = "text")
    private String parsedText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> summary;

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