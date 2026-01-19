package com.project_x.project_x_backend.dto.jobDTO;

import com.project_x.project_x_backend.enums.JobStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobPollingRes {
    private JobStatus status;
    private String errorMessage;
    private String stt;
    private String noteback;
}
