package com.project_x.project_x_backend.controller;

import com.project_x.project_x_backend.dto.AuthResponse;
import com.project_x.project_x_backend.dto.UserResponse;
import com.project_x.project_x_backend.entity.User;
import com.project_x.project_x_backend.service.JwtService;
import com.project_x.project_x_backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    /**
     * OAuth2 Success Handler - called after successful Google authentication
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            // Create or update user from OAuth2 data
            User user = userService.createOrUpdateFromOAuth(oAuth2User);

            // Generate JWT token
            String token = jwtService.generateToken(user.getEmail(), user.getName(), user.getId());

            // Create response
            AuthResponse response = new AuthResponse(
                    token,
                    "Bearer",
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getProfilePictureUrl());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current user information from JWT token
     */
    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            if (!authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authorization.substring(7);

            if (!jwtService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UUID userId = jwtService.extractUserId(token);
            Optional<User> userOpt = userService.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserResponse userResponse = new UserResponse(userOpt.get());
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            if (!authorization.startsWith("Bearer ")) {
                return ResponseEntity.ok(false);
            }

            String token = authorization.substring(7);
            boolean isValid = jwtService.isTokenValid(token);

            return ResponseEntity.ok(isValid);

        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * OAuth2 login initiation endpoint
     */
    @GetMapping("/login/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
}