package com.example.api.entity;

import com.example.api.entity.enums.Status;
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
        name = "exams",
        schema = "app",
        indexes = {
                @Index(name = "idx_exams_course_created_at", columnList = "course_id, created_at"),
                @Index(name = "idx_exams_course_updated_at", columnList = "course_id, updated_at")
        }
)
@Check(constraints = "status IN ('generate_in_progress', 'not_started', 'submitted', 'partially_graded', 'graded')")
public class Exam {
    @Id
    @Column()
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    @Column(name = "referenced_lectures")
    private UUID[] referencedLectures;

    @Column(name = "contents_generated_at")
    private LocalDateTime contentsGenerateAt;

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
