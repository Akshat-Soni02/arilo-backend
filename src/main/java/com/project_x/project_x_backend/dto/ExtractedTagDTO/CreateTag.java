package com.project_x.project_x_backend.dto.ExtractedTagDTO;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;

@Component
@Data
@AllArgsConstructor
public class CreateTag {
    private UUID jobId;
    private UUID userId;
    private String tag;
    private Integer tagCount;
}
