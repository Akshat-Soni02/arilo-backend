package com.project_x.project_x_backend.dto.pipelineDTO;

import java.util.UUID;


import com.project_x.project_x_backend.enums.PipelineName;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePipeline {
    private UUID jobId;
    private PipelineName pipelineName;
}
