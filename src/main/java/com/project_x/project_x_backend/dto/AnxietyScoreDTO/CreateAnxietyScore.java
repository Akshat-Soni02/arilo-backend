package com.project_x.project_x_backend.dto.AnxietyScoreDTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAnxietyScore {
    private UUID jobId;
    private UUID userId;
    private Integer anxietyScore;
}
