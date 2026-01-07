package com.project_x.project_x_backend.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;

import com.project_x.project_x_backend.enums.PipelineName;
import com.project_x.project_x_backend.enums.PipelineStageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Getter
@Setter
@Entity
@Table(name = "pipeline_stages")
public class PipelineStage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_name", nullable = false)
    private PipelineName pipelineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PipelineStageStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
