package com.project_x.project_x_backend.dto.ExtractedTaskDTO;

import com.project_x.project_x_backend.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskReq {
    private String task;
    private TaskStatus status;
}
