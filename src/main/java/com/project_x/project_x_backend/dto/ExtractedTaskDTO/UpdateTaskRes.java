package com.project_x.project_x_backend.dto.ExtractedTaskDTO;

import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateTaskRes {
    private UUID id;
    private String task;
    private TaskStatus status;
    private Instant createdAt;
}
