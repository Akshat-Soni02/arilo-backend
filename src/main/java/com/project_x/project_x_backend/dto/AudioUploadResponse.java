package com.project_x.project_x_backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project_x.project_x_backend.entity.AudioStore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AudioUploadResponse {
    private UUID audioId;
    private int durationSeconds;
    private String storageUrl;
    private AudioStore.Status status;
    private LocalDateTime uploadedAt;

    public AudioUploadResponse() {
    }

    public AudioUploadResponse(AudioStore audioStore) {
        this.audioId = audioStore.getId();
        this.durationSeconds = audioStore.getDurationSeconds();
        this.storageUrl = audioStore.getStorageUrl();
        this.status = audioStore.getStatus();
        this.uploadedAt = audioStore.getCreatedAt();
    }

    public UUID getAudioId() {
        return audioId;
    }

    public void setAudioId(UUID audioId) {
        this.audioId = audioId;
    }

    public AudioStore.Status getStatus() {
        return status;
    }

    public void setStatus(AudioStore.Status status) {
        this.status = status;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}