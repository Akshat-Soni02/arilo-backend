package com.project_x.project_x_backend.dto.TagDTO;

import com.project_x.project_x_backend.enums.TagSource;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagCreateResponse {
    private UUID tagId;
    private String name;
    private String description;
    private TagSource createdBy;
    private Instant createdAt;
}
