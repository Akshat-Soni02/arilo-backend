package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.dao.SubscriptionDAO;
import com.project_x.project_x_backend.dao.UsageCycleDAO;
import com.project_x.project_x_backend.dao.UserDailyUsageDAO;
import com.project_x.project_x_backend.entity.UserDailyUsage;
import com.project_x.project_x_backend.dto.LimitRes;
import com.project_x.project_x_backend.dto.SubscriptionDTO.CreateSubscription;
import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.UsageCycle;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.enums.LimitStatus;
import com.project_x.project_x_backend.enums.PlanTypes;
import com.project_x.project_x_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionDAO subscriptionDAO;

    @Autowired
    private UserDailyUsageDAO userDailyUsageDAO;

    @Autowired
    private UsageCycleDAO usageCycleDAO;

    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findActiveUserByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findActiveUserById(id);
    }

    public Optional<User> findByGoogleId(String googleId) {
        log.debug("Finding user by googleId: {}", googleId);
        return userRepository.findActiveUserByGoogleId(googleId);
    }

    public LimitRes getUsages(UUID userId) {
        Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(userId);
        if (!subscription.isPresent()) {
            throw new RuntimeException("No active subscription found for user");
        }

        Plan plan = subscription.get().getPlan();
        UsageCycle usageCycle = usageCycleDAO.getCurrentCycle(userId, subscription.get());
        UserDailyUsage userDailyUsage = userDailyUsageDAO.getDailyUsage(userId);

        LimitStatus status = LimitStatus.OK;
        if ((userDailyUsage.getNotesUsed() >= plan.getNoteDailyLimit() && plan.getNoteDailyLimit() != -1)
                || (usageCycle.getNotesUsed() >= plan.getNoteMonthlyLimit() && plan.getNoteMonthlyLimit() != -1)) {
            status = LimitStatus.REACHED;
        }
        return new LimitRes(plan.getNoteDailyLimit(), userDailyUsage.getNotesUsed(), plan.getNoteMonthlyLimit(),
                usageCycle.getNotesUsed(), status);
    }

    public User createOrUpdateFromOAuth(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String profilePictureUrl = oAuth2User.getAttribute("picture");

        log.info("Creating or updating user from OAuth: {}", email);
        try {
            Optional<User> existingUser = userRepository.findActiveUserByGoogleId(googleId);

            if (existingUser.isPresent()) {
                log.debug("Updating existing user: {}", email);
                User user = existingUser.get();
                user.setEmail(email);
                user.setName(name);
                user.setProfilePictureUrl(profilePictureUrl);
                return userRepository.save(user);
            } else {
                log.info("Creating new user from OAuth: {}", email);
                User newUser = new User(email, googleId, name, profilePictureUrl);
                return userRepository.save(newUser);
            }
        } catch (Exception e) {
            log.error("Failed to create or update user from OAuth for {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    public User createOrUpdateFromGooglePayload(Payload payload) {
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");
        String profilePictureUrl = (String) payload.get("picture");

        log.info("Creating or updating user from Google payload: {}", email);
        try {
            Optional<User> existingUser = userRepository.findActiveUserByGoogleId(googleId);

            // if user is existing, check if they have a valid subscription
            // if they don't have a valid subscription, create free subscription for the
            // user

            if (existingUser.isPresent()) {
                log.debug("User {} exists, checking subscription status", email);
                UUID userId = existingUser.get().getId();
                Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(userId);

                if (!subscription.isPresent()) {
                    log.info("User {} has no active subscription, creating FREE plan subscription", email);
                    subscriptionDAO.createSubscription(new CreateSubscription(userId, PlanTypes.FREE));
                }

                User user = existingUser.get();
                user.setEmail(email);
                user.setName(name);
                user.setProfilePictureUrl(profilePictureUrl);
                return userRepository.save(user);
            } else {
                log.info("Creating new user from Google payload: {}", email);
                User newUser = new User(email, googleId, name, profilePictureUrl);
                newUser = userRepository.save(newUser);
                log.info("Newly created user ID: {}, initializing FREE subscription", newUser.getId());
                subscriptionDAO.createSubscription(new CreateSubscription(newUser.getId(), PlanTypes.FREE));
                return newUser;
            }
        } catch (Exception e) {
            log.error("Failed to create or update user from Google payload for {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }
}
