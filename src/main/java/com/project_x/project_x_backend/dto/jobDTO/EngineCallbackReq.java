package com.project_x.project_x_backend.dto.jobDTO;

import java.sql.Timestamp;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.project_x.project_x_backend.enums.PipelineName;
import com.project_x.project_x_backend.enums.PipelineStageStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EngineCallbackReq {

    @NotNull
    @JsonProperty("job_id")
    private UUID jobId;

    @NotNull
    @JsonProperty("note_id")
    private UUID noteId;

    @NotNull
    @JsonProperty("user_id")
    private UUID userId;

    @NotNull
    @JsonProperty("input_type")
    private String inputType;

    @NotNull
    private String location;

    @NotNull
    private Timestamp timestamp;

    @NotNull
    private JsonNode output;

    @NotNull
    private String error;

    @NotNull
    @JsonProperty("pipeline_stage")
    private PipelineName pipelineStage;

    @NotNull
    private PipelineStageStatus status;
}
