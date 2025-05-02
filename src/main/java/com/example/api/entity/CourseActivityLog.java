package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;



@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "course_activity_logs",
        schema = "app",
        indexes = {
                @Index(name = "idx_course_activity_logs_course_created_at", columnList = "course_id, created_at")
        }
)
public class CourseActivityLog {
    @Id
    @Column()
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name = "contents_type", nullable = false)
    private String contentsType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activity_details", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> activityDetails;
    // private String activityDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
