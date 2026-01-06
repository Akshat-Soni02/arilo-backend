package com.project_x.project_x_backend.dto;

import java.time.LocalDateTime;

import com.project_x.project_x_backend.entity.Note;

import java.util.UUID;

public class AudioUploadResponse {
    private UUID noteId;
    private int durationSeconds;
    private String storageUrl;
    private Note.Status status;
    private LocalDateTime uploadedAt;

    public AudioUploadResponse() {
    }

    public AudioUploadResponse(Note note) {
        this.noteId = note.getId();
        this.durationSeconds = note.getDurationSeconds();
        this.storageUrl = note.getStorageUrl();
        this.status = note.getStatus();
        this.uploadedAt = note.getCreatedAt();
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public Note.Status getStatus() {
        return status;
    }

    public void setStatus(Note.Status status) {
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