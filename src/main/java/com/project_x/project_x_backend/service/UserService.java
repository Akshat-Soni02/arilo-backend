package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

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
}
