package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notes")
@Data
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "storage_url", nullable = true)
    private String storageUrl;

    @Column(name = "duration_seconds", nullable = true)
    private int durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "note_type", nullable = false)
    private String noteType;

    @Column(name = "text_content", columnDefinition = "text")
    private String textContent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Enum for processing status
    public enum Status {
        PROCESSING, DELETED, FAILED, UPLOADED
    }

    public Note() {
    }

    public Note(UUID userId, String storageUrl, int durationSeconds, Status status, String noteType,
            String textContent) {
        this.userId = userId;
        this.storageUrl = storageUrl;
        this.durationSeconds = durationSeconds;
        this.status = status;
        this.noteType = noteType;
        this.textContent = textContent;
    }
}
