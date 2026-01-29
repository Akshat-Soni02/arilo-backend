package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.UserDailyUsage;
import com.project_x.project_x_backend.repository.UserDailyUsageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import javax.naming.LimitExceededException;

// TODO: increment daily usage when note is uploaded and reduce if note failed
// TODO: while reducing, handle the case when note way submitted to prior day but when engine callback came the day changed

@Component
@Slf4j
public class UserDailyUsageDAO {

    @Autowired
    private UserDailyUsageRepository userDailyUsageRepository;

    @Autowired
    private SubscriptionDAO subscriptionDAO;

    public UserDailyUsage getDailyUsage(UUID userId) {
        log.debug("Getting daily usage for user {}", userId);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Optional<UserDailyUsage> userDailyUsage = userDailyUsageRepository.findByUserIdAndUsageDate(userId, today);
        if (!userDailyUsage.isPresent()) {
            userDailyUsageRepository.ensureDailyUsageRow(userId, today);
            userDailyUsage = userDailyUsageRepository.findByUserIdAndUsageDate(userId, today);
        }
        return userDailyUsage.get();
    }

    @Transactional
    public void consumeDailyUsage(UUID userId) throws LimitExceededException {
        log.debug("Incrementing daily usage for user {}", userId);

        Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(userId);
        if (!subscription.isPresent()) {
            throw new RuntimeException("No active subscription found for user");
        }

        Plan plan = subscription.get().getPlan();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        userDailyUsageRepository.ensureDailyUsageRow(userId, today);

        int updated = userDailyUsageRepository.incrementUsage(userId, today, plan.getNoteDailyLimit());
        if (updated == 0) {
            throw new LimitExceededException("Daily limit reached");
        }
    }
}
