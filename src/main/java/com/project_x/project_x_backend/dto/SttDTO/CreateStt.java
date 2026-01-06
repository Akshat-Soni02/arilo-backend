package com.project_x.project_x_backend.dto.SttDTO;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStt {
    private UUID jobId;
    private UUID userId;
    private String language;
    private String stt;
}
