package com.project_x.project_x_backend.dto.SubscriptionDTO;

import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.PlanTypes;
import com.project_x.project_x_backend.enums.SubscriptionStatus;
import com.project_x.project_x_backend.entity.Subscription;

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

    private PlanTypes planName;
    private Integer planDuration;
    private Integer noteDailyLimit;
    private Integer noteMonthlyLimit;

    public SubscriptionDetailDTO(Subscription subscription) {
        if (subscription != null) {
            this.id = subscription.getId();
            this.userId = subscription.getUser().getId();
            this.planId = subscription.getPlan().getId();
            this.status = subscription.getStatus();
            this.startDate = subscription.getStartDate();
            this.endDate = subscription.getEndDate();
            this.autoRenew = subscription.getAutoRenew();
            this.cancelledAt = subscription.getCancelledAt();
            this.createdAt = subscription.getCreatedAt();
            this.updatedAt = subscription.getUpdatedAt();
            this.planName = subscription.getPlan().getName();
            this.planDuration = subscription.getPlan().getDurationDays();
            this.noteDailyLimit = subscription.getPlan().getNoteDailyLimit();
            this.noteMonthlyLimit = subscription.getPlan().getNoteMonthlyLimit();
        }
    }
}
