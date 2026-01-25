package com.project_x.project_x_backend.dto.jobDTO;

import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.JobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailRes {
    private UUID id;
    private UUID userId;
    private UUID noteId;
    private JobStatus status;
    private String error;
    private Instant createdAt;
    private Instant updatedAt;
}
