package com.project_x.project_x_backend.dto.NoteDTO;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;

@Data
public class NoteRes {
    private UUID noteId;
    private String stt;
    private String noteback;
    private Instant createdAt;
}
