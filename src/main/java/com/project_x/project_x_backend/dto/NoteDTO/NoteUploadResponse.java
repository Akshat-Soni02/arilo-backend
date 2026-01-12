package com.project_x.project_x_backend.dto.NoteDTO;

import java.util.UUID;

import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.enums.NoteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteUploadResponse {
    private UUID noteId;
    private NoteType noteType;
    private UUID jobId;
    private JobStatus status;
}