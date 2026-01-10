package com.project_x.project_x_backend.dto;

import java.time.Instant;
import java.util.UUID;
import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.enums.NoteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadResponse {
    private UUID noteId;
    private int durationSeconds;
    private String storageUrl;
    private NoteStatus status;
    private Instant uploadedAt;

    public AudioUploadResponse(Note note) {
        this.noteId = note.getId();
        this.durationSeconds = note.getDurationSeconds();
        this.storageUrl = note.getStorageUrl();
        this.status = note.getStatus();
        this.uploadedAt = note.getCreatedAt();
    }
}