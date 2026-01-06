package com.project_x.project_x_backend.dto.jobDTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateJob {
    private UUID userId;
    private UUID noteId;
}
