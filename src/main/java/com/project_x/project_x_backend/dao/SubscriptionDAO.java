package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.dto.SubscriptionDTO.CreateSubscription;
import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.enums.SubscriptionStatus;
import com.project_x.project_x_backend.repository.PlanRepository;
import com.project_x.project_x_backend.repository.SubscriptionRepository;
import com.project_x.project_x_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class SubscriptionDAO {

        @Autowired
        private SubscriptionRepository subscriptionRepository;

        @Autowired
        private PlanRepository planRepository;

        @Autowired
        private UserRepository userRepository;

        public List<Subscription> getAllSubscriptions() {
                return subscriptionRepository.findAll();
        }

        @Transactional
        public Subscription createSubscription(CreateSubscription request) {
                log.info("Creating {} subscription for user {}", request.getPlanType(), request.getUserId());
                try {
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

                        Subscription saved = subscriptionRepository.save(subscription);
                        log.info("Successfully created subscription ID: {} for user {}", saved.getId(),
                                        request.getUserId());
                        return saved;
                } catch (Exception e) {
                        log.error("Failed to create subscription for user {}: {}", request.getUserId(), e.getMessage(),
                                        e);
                        throw e;
                }
        }

        @Transactional
        public void cancelSubscription(UUID subscriptionId) {
                log.info("Cancelling subscription ID: {}", subscriptionId);
                try {
                        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                                        .orElseThrow(() -> new RuntimeException("Subscription not found"));

                        subscription.setStatus(SubscriptionStatus.CANCELLED);
                        subscription.setCancelledAt(Instant.now());
                        subscription.setEndDate(Instant.now()); // Expire immediately as requested by "cancel"
                        subscriptionRepository.save(subscription);
                        log.info("Successfully cancelled subscription ID: {}", subscriptionId);
                } catch (Exception e) {
                        log.error("Failed to cancel subscription {}: {}", subscriptionId, e.getMessage(), e);
                        throw e;
                }
        }

        @Transactional
        public void expireSubscription(UUID subscriptionId) {
                log.info("Expiring subscription ID: {}", subscriptionId);
                try {
                        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                                        .orElseThrow(() -> new RuntimeException("Subscription not found"));

                        subscription.setStatus(SubscriptionStatus.EXPIRED);
                        subscription.setEndDate(Instant.now());
                        subscriptionRepository.save(subscription);
                        log.info("Successfully expired subscription ID: {}", subscriptionId);
                } catch (Exception e) {
                        log.error("Failed to expire subscription {}: {}", subscriptionId, e.getMessage(), e);
                        throw e;
                }
        }

        public List<Subscription> getAllUserSubscriptions(UUID userId) {
                log.debug("Fetching all subscriptions for user {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return subscriptionRepository.findAllByUserOrderByCreatedAtDesc(user);
        }

        public Optional<Subscription> getUserActiveSubscription(UUID userId) {
                log.debug("Fetching active subscription for user {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return subscriptionRepository.findFirstByUserAndStatusAndEndDateAfterOrderByCreatedAtDesc(
                                user, SubscriptionStatus.ACTIVE, Instant.now());
        }
}
