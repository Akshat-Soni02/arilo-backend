package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.dto.SubscriptionDTO.CreateSubscription;
import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.enums.SubscriptionStatus;
import com.project_x.project_x_backend.repository.PlanRepository;
import com.project_x.project_x_backend.repository.SubscriptionRepository;
import com.project_x.project_x_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionDAO {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Subscription createSubscription(CreateSubscription request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Plan plan = planRepository.findByNameAndIsActiveTrue(request.getPlanType())
                .orElseThrow(() -> new RuntimeException("Plan not found or inactive"));

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(Instant.now());
        subscription.setEndDate(Instant.now().plus(plan.getDurationDays(), ChronoUnit.DAYS));
        subscription.setAutoRenew(false);

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(Instant.now());
        subscription.setEndDate(Instant.now()); // Expire immediately as requested by "cancel"
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void expireSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setEndDate(Instant.now());
        subscriptionRepository.save(subscription);
    }

    public List<Subscription> getAllUserSubscriptions(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return subscriptionRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Subscription> getUserActiveSubscription(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return subscriptionRepository.findFirstByUserAndStatusAndEndDateAfterOrderByCreatedAtDesc(
                user, SubscriptionStatus.ACTIVE, Instant.now());
    }
}
