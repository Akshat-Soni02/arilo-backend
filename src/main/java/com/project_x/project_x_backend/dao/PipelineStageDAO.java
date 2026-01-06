package com.project_x.project_x_backend.dao;

import java.util.List;
import java.util.UUID;

import com.project_x.project_x_backend.dto.pipelineDTO.CreatePipeline;
import com.project_x.project_x_backend.enums.PipelineStageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import com.project_x.project_x_backend.repository.PipelineStageRepository;
import com.project_x.project_x_backend.entity.PipelineStage;

import org.springframework.stereotype.Component;

@Component
public class PipelineStageDAO {
    @Autowired
    private PipelineStageRepository pipelineStageRepository;

    public PipelineStage createPipelineStage(CreatePipeline createPipeline) {
        PipelineStage pipelineStage = new PipelineStage();
        pipelineStage.setJobId(createPipeline.getJobId());
        pipelineStage.setAttemptCount(0);
        pipelineStage.setStatus(PipelineStageStatus.PENDING);
        pipelineStage.setPipelineName(createPipeline.getPipelineName());
        return pipelineStageRepository.save(pipelineStage);
    }

    public List<PipelineStage> getPipelineStagesByJobId(UUID jobId) {
        return pipelineStageRepository.findByJobId(jobId);
    }

}
