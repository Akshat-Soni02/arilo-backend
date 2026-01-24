package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<Subscription> findFirstByUserAndStatusAndEndDateAfterOrderByCreatedAtDesc(
            User user, SubscriptionStatus status, Instant now);
}
