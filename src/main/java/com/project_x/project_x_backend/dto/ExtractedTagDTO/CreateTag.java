package com.project_x.project_x_backend.dto.ExtractedTagDTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateTag {
    private UUID jobId;
    private UUID userId;
    private String tag;
}
