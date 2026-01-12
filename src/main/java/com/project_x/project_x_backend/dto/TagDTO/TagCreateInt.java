package com.project_x.project_x_backend.dto.TagDTO;

import java.util.UUID;
import com.project_x.project_x_backend.enums.TagSource;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagCreateInt {
    private UUID userId;
    private String name;
    private String description;
    private TagSource createdBy;
}
