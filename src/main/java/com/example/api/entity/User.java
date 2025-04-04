package com.example.api.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Check;

import com.example.api.entity.enums.AuthType;

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
    name = "users",
    schema = "app",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_google_id", columnList = "google_id")
    }
)
@Check(constraints = "auth_type IN ('email', 'goolgle')")
public class User {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "school_id")
    //private School school;
    
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "google_id", length = 100)
    private String googleId;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 20, columnDefinition = "varchar(20) default 'email'")
    private AuthType authType;    // Enum(email, google)으로 관리

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
        if (isActive == null) {
            isActive = true;
        }
        if (authType == null) {
            authType = AuthType.email;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
