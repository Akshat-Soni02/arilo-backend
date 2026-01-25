package com.project_x.project_x_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret:myDefaultSecretKeyThatIsLongEnoughForHS256Algorithm}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email, String name, UUID userId) {
        log.info("Generating JWT for user: {}", email);
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtExpiration, ChronoUnit.MILLIS);

        try {
            String token = Jwts.builder()
                    .subject(email)
                    .claim("name", name)
                    .claim("userId", userId.toString())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiry))
                    .signWith(getSigningKey())
                    .compact();
            log.info("Successfully generated JWT for user: {}", email);
            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT for user {}: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    public String extractEmail(String token) {
        log.debug("Extracting email from JWT");
        return extractClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        log.debug("Extracting userId from JWT");
        String userIdStr = extractClaims(token).get("userId", String.class);
        return UUID.fromString(userIdStr);
    }

    public boolean isTokenValid(String token) {
        log.debug("Validating JWT");
        try {
            Claims claims = extractClaims(token);
            boolean isValid = claims.getExpiration().after(new Date());
            if (!isValid) {
                log.warn("JWT validation failed: token is expired");
            }
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage(), e);
            return false;
        }
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("Failed to extract claims from JWT: {}", e.getMessage());
            throw e;
        }
    }
}
