package com.project_x.project_x_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private JwtService jwtService;

    public UUID extractUserIdFromToken(String authorization) {
        log.info("Extracting user ID from authorization header");
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("Authorization header is missing or does not start with 'Bearer '");
                return null;
            }

            String token = authorization.substring(7);
            if (!jwtService.isTokenValid(token)) {
                log.warn("JWT validation failed during user ID extraction");
                return null;
            }

            UUID userId = jwtService.extractUserId(token);
            log.info("Successfully extracted user ID: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Unexpected error while extracting user ID from token: {}", e.getMessage(), e);
            return null;
        }
    }
}
