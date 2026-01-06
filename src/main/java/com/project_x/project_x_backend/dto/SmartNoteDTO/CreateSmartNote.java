package com.project_x.project_x_backend.dto.SmartNoteDTO;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Component
@Data
@AllArgsConstructor
public class CreateSmartNote {
    private UUID jobId;
    private UUID userId;
    private String note;
    private JsonNode metadata;
}
