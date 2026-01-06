package com.project_x.project_x_backend.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private JwtService jwtService;

    public UUID extractUserIdFromToken(String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header");
                return null;
            }

            String token = authorization.substring(7);
            if (!jwtService.isTokenValid(token)) {
                logger.warn("Invalid JWT token");
                return null;
            }

            return jwtService.extractUserId(token);
        } catch (Exception e) {
            logger.error("Error extracting user ID: {}", e.getMessage());
            return null;
        }
    }

}
