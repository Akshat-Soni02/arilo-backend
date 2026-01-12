package com.project_x.project_x_backend.entity;

import com.project_x.project_x_backend.enums.NoteStatus;
import com.project_x.project_x_backend.enums.NoteType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "storage_url", nullable = true)
    private String storageUrl;

    @Column(name = "duration_seconds", nullable = true)
    private Integer durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NoteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false)
    private NoteType noteType;

    @Column(name = "text_content", columnDefinition = "text")
    private String textContent;

    @OneToOne(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stt stt;

    @OneToOne(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Noteback noteback;

    @OneToOne(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnxietyScore anxietyScore;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExtractedTag> extractedTags;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExtractedTask> extractedTasks;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NoteTag> noteTags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Note(UUID userId, String storageUrl, int durationSeconds, NoteStatus status, NoteType noteType,
            String textContent) {
        this.userId = userId;
        this.storageUrl = storageUrl;
        this.durationSeconds = durationSeconds;
        this.status = status;
        this.noteType = noteType;
        this.textContent = textContent;
    }
}
