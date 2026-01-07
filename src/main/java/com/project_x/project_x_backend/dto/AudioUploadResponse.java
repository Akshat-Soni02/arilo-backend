package com.project_x.project_x_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.enums.NoteStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AudioUploadResponse {
    private UUID noteId;
    private int durationSeconds;
    private String storageUrl;
    private NoteStatus status;
    private LocalDateTime uploadedAt;

    public AudioUploadResponse(Note note) {
        this.noteId = note.getId();
        this.durationSeconds = note.getDurationSeconds();
        this.storageUrl = note.getStorageUrl();
        this.status = note.getStatus();
        this.uploadedAt = note.getCreatedAt();
    }
}