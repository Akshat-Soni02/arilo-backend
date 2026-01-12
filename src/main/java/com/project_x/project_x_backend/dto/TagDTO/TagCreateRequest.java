package com.project_x.project_x_backend.dto.TagDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagCreateRequest {
    private String name;
    private String description;
}
