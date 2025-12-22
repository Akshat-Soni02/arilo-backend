package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity 
@Table(name= "audio_store")
public class AudioStore {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id",nullable = false)
    private UUID userId;

    @Column(name = "gcs_audio_url",nullable=true)
    private String gcsAudioUrl;

    @Column(name= "duration_seconds", nullable = true)
    private int durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name="processing_status", nullable = false)
    private ProcessingStatus processingStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Enum for processing status
    public enum ProcessingStatus {
        PENDING, PROCESSING, DELETED, FAILED, UPLOADED
    }
    
    public AudioStore() {}

    public AudioStore(UUID userId, String gcsAudioUrl, int durationSeconds, ProcessingStatus processingStatus) {
        this.userId = userId;
        this.gcsAudioUrl = gcsAudioUrl;
        this.durationSeconds = durationSeconds;
        this.processingStatus = processingStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }    

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getGcsAudioUrl() { return gcsAudioUrl; }
    public void setGcsAudioUrl(String gcsAudioUrl) { this.gcsAudioUrl = gcsAudioUrl; }   
    
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public ProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(ProcessingStatus processingStatus) { this.processingStatus = processingStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt() { this.createdAt = LocalDateTime.now(); }
}
