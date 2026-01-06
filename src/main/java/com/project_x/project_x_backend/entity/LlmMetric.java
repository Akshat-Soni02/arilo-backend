package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.sql.Timestamp;

@Entity
@Table(name = "llm_metrics")
@Data
public class LlmMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_stage_id", nullable = false)
    private PipelineStage pipelineStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_output_id", nullable = false)
    private PipelineOutput pipelineOutput;

    @Column(name = "llm_call", nullable = false)
    private String llmCall;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "total_input_tokens")
    private Integer totalInputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "thought_tokens")
    private Integer thoughtTokens;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "elapsed_time")
    private Double elapsedTime;

    private String model;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
