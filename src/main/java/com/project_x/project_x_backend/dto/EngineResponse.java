package com.project_x.project_x_backend.dto;

import com.google.api.client.json.Json;

import lombok.Data;

@Data
public class EngineResponse {
    private String job_id;
    private String note_id;
    private String user_id;
    private String location;
    private String status;
    private String timestamp;
    private String input_type;
    private String error;
    private String pipeline_stage;
    private Json output;
}
