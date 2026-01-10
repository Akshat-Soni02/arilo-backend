package com.project_x.project_x_backend.dto.TagDTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagUpdateInt {
    private UUID userId;
    private UUID tagId;
    private String name;
    private String description;
}
