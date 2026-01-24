package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.dao.SubscriptionDAO;
import com.project_x.project_x_backend.dto.SubscriptionDTO.CreateSubscription;
import com.project_x.project_x_backend.entity.Subscription;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.enums.PlanTypes;
import com.project_x.project_x_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionDAO subscriptionDAO;

    public Optional<User> findByEmail(String email) {
        return userRepository.findActiveUserByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findActiveUserById(id);
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findActiveUserByGoogleId(googleId);
    }

    public User createOrUpdateFromOAuth(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String profilePictureUrl = oAuth2User.getAttribute("picture");

        Optional<User> existingUser = userRepository.findActiveUserByGoogleId(googleId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setEmail(email);
            user.setName(name);
            user.setProfilePictureUrl(profilePictureUrl);
            return userRepository.save(user);
        } else {

            User newUser = new User(email, googleId, name, profilePictureUrl);
            return userRepository.save(newUser);
        }
    }

    public User createOrUpdateFromGooglePayload(
            Payload payload) {
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");
        String profilePictureUrl = (String) payload.get("picture");

        Optional<User> existingUser = userRepository.findActiveUserByGoogleId(googleId);

        // if user is existing, check if they have a valid subscription
        // if they don't have a valid subscription, create free subscription for the
        // user

        if (existingUser.isPresent()) {
            Optional<Subscription> subscription = subscriptionDAO.getUserActiveSubscription(existingUser.get().getId());

            if (!subscription.isPresent()) {
                subscriptionDAO.createSubscription(new CreateSubscription(existingUser.get().getId(), PlanTypes.FREE));
            }

            User user = existingUser.get();
            user.setEmail(email);
            user.setName(name);
            user.setProfilePictureUrl(profilePictureUrl);
            return userRepository.save(user);
        } else {
            User newUser = new User(email, googleId, name, profilePictureUrl);
            userRepository.save(newUser);
            subscriptionDAO.createSubscription(new CreateSubscription(newUser.getId(), PlanTypes.FREE));
            return newUser;
        }
    }
}
