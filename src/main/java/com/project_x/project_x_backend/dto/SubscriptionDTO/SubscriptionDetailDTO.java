package com.project_x.project_x_backend.dto.SubscriptionDTO;

import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDetailDTO {
    private UUID id;
    private UUID userId;
    private UUID planId;
    private SubscriptionStatus status;
    private Instant startDate;
    private Instant endDate;
    private Boolean autoRenew;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
