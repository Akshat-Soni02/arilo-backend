package com.project_x.project_x_backend.dto.NoteDTO;

import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.enums.NoteType;

import lombok.Data;

@Data
public class NoteRes {
    private UUID noteId;
    private NoteType noteType;
    private String stt;
    private UUID jobId;
    private JobStatus status;
    private String noteback;
    private Instant createdAt;
}
