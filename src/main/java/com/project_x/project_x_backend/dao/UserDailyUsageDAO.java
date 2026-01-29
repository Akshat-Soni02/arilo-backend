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

    @Transactional
    public void reduceDailyUsage(UUID userId) {
        log.debug("Reducing daily usage for user {}", userId);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int updated = userDailyUsageRepository.decrementUsage(userId, today);
        if (updated == 0) {
            log.debug("No daily usage found for user {}", userId);
            log.debug("Consider it a previous day job and reducing previous day limit");
            userDailyUsageRepository.decrementUsage(userId, today.minusDays(1));
        }
    }
}
