package com.project_x.project_x_backend.dto.ExtractedTaskDTO.test;

import com.project_x.project_x_backend.enums.TaskStatus;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestCreateTask {
    private UUID jobId;
    private UUID userId;
    private String task;
    private TaskStatus status;
}
