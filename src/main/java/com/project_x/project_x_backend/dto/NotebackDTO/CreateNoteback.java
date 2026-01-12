package com.project_x.project_x_backend.dto.NotebackDTO;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNoteback {
    private UUID jobId;
    private UUID userId;
    private String note;
    private JsonNode metadata;
}
