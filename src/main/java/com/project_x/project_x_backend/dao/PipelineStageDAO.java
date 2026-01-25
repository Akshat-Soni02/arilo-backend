package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.dto.pipelineDTO.CreatePipeline;
import com.project_x.project_x_backend.enums.PipelineStageStatus;
import com.project_x.project_x_backend.repository.PipelineStageRepository;
import com.project_x.project_x_backend.entity.PipelineStage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class PipelineStageDAO {
    @Autowired
    private PipelineStageRepository pipelineStageRepository;

    public PipelineStage createPipelineStage(CreatePipeline createPipeline) {
        log.info("Creating {} pipeline stage for job {}", createPipeline.getPipelineName(), createPipeline.getJobId());
        try {
            PipelineStage pipelineStage = new PipelineStage();
            pipelineStage.setJobId(createPipeline.getJobId());
            pipelineStage.setAttemptCount(0);
            pipelineStage.setStatus(PipelineStageStatus.PENDING);
            pipelineStage.setPipelineName(createPipeline.getPipelineName());
            PipelineStage saved = pipelineStageRepository.save(pipelineStage);
            log.info("Successfully created pipeline stage ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create pipeline stage for job {}: {}", createPipeline.getJobId(), e.getMessage(), e);
            throw e;
        }
    }

    public List<PipelineStage> getPipelineStagesByJobId(UUID jobId) {
        log.debug("Fetching all pipeline stages for job {}", jobId);
        return pipelineStageRepository.findByJobId(jobId);
    }
}
