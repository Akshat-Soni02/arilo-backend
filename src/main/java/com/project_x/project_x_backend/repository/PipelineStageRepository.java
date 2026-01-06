package com.project_x.project_x_backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project_x.project_x_backend.entity.PipelineStage;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {
    List<PipelineStage> findByJobId(UUID jobId);
}
