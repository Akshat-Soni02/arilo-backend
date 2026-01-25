package com.project_x.project_x_backend.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailDTO {
    private UUID id;
    private String email;
    private String googleId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
