package com.project_x.project_x_backend.dto.SubscriptionDTO;

import com.project_x.project_x_backend.enums.PlanTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscription {
    private UUID userId;
    private PlanTypes planType;
}
