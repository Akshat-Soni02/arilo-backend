package com.project_x.project_x_backend.dto.jobDTO.test;

import com.project_x.project_x_backend.enums.JobStatus;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestCreateJob {
    private UUID userId;
    private UUID noteId;
    private JobStatus status;
}
