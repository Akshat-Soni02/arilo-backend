package com.project_x.project_x_backend.dto.LLM;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmMetricDetailDTO {
    private UUID id;
    private UUID userId;
    private UUID jobId;
    private UUID pipelineStageId;
    private String llmCall;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer promptTokens;
    private Integer totalInputTokens;
    private Integer thoughtTokens;
    private Double confidenceScore;
    private Double elapsedTime;
    private String model;
    private Instant createdAt;
    private Instant updatedAt;
}
