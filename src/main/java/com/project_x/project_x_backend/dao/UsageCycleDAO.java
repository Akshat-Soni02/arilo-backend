package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.UsageCycle;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.repository.UsageCycleRepository;
import com.project_x.project_x_backend.repository.UserRepository;
import com.project_x.project_x_backend.dao.SubscriptionDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.time.Duration;

import javax.naming.LimitExceededException;

@Component
@Slf4j
public class UsageCycleDAO {

    @Autowired
    private UsageCycleRepository usageCycleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionDAO subscriptionDAO;

    public Optional<UsageCycle> findByUserIdAndCycleStart(UUID userId, Instant cycleStart) {
        log.debug("Finding usage cycle for user {} and start time {}", userId, cycleStart);
        return usageCycleRepository.findByUserIdAndCycleStart(userId, cycleStart);
    }

    public UsageCycle fetchOrCreateUsageCycle(UUID userId, Instant cycleStart, Instant cycleEnd) {
        log.debug("Fetching or creating usage cycle for user {}", userId);
        UsageCycle cycle = usageCycleRepository.findByUserIdAndCycleStart(userId, cycleStart)
                .orElseGet(() -> createUsageCycle(userId, cycleStart, cycleEnd));
        return cycle;
    }

    public UsageCycle createUsageCycle(UUID userId, Instant cycleStart, Instant cycleEnd) {
        log.info("Creating usage cycle for user {} from {} to {}", userId, cycleStart, cycleEnd);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UsageCycle usageCycle = new UsageCycle();
        usageCycle.setUser(user);
        usageCycle.setCycleStart(cycleStart);
        usageCycle.setCycleEnd(cycleEnd);
        usageCycle.setNotesUsed(0);

        return usageCycleRepository.save(usageCycle);
    }

    @Transactional
    public void consumeCycleUsage(UUID userId) throws LimitExceededException {
        log.debug("Incrementing daily usage for user {}", userId);

        Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(userId);
        if (!subscription.isPresent()) {
            throw new RuntimeException("No active subscription found");
        }

        Plan plan = subscription.get().getPlan();
        UsageCycle usageCycle = getCurrentCycle(userId, subscription.get());

        int updated = usageCycleRepository.incrementUsage(usageCycle.getId(), plan.getNoteMonthlyLimit());
        if (updated == 0) {
            throw new LimitExceededException("Cycle limit reached");
        }
    }

    @Transactional
    public void reduceCycleUsage(UUID userId) {
        log.debug("Reducing daily usage for user {}", userId);
        UsageCycle usageCycle = getCurrentCycle(userId, subscriptionDAO.getUserActiveSubscription(userId).get());
        usageCycleRepository.decrementUsage(usageCycle.getId());
    }

    public UsageCycle getCurrentCycle(UUID userId, Subscription subscription) {
        Instant cycleStart = subscription.getStartDate();
        long daysSinceStart = Duration.between(cycleStart, Instant.now()).toDays();

        long cycleIndex = daysSinceStart / 30;

        Instant currentCycleStart = cycleStart.plus(cycleIndex * 30, ChronoUnit.DAYS);
        Instant currentCycleEnd = currentCycleStart.plus(30, ChronoUnit.DAYS);

        return fetchOrCreateUsageCycle(userId, currentCycleStart, currentCycleEnd);
    }
}
