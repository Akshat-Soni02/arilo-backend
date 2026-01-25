package com.project_x.project_x_backend.dto.PlanDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.PlanTypes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanDetailDTO {
    private UUID id;
    private PlanTypes name;
    private BigDecimal price;
    private String currency;
    private Integer durationDays;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
