package com.project_x.project_x_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

@Component
@Slf4j
public class AdminSecurityFilter extends OncePerRequestFilter {

    @Value("${app.admin.secret}")
    private String adminSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.startsWith("/admin")) {
            String signature = request.getHeader("X-Admin-Signature");
            String timestamp = request.getHeader("X-Admin-Timestamp");

            if (signature != null && timestamp != null) {
                if (isSignatureValid(signature, timestamp)) {
                    log.info("Admin access granted via Signature for path: {}", path);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            "ADMIN_USER", null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("Invalid admin signature attempt for path: {}", path);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSignatureValid(String signature, String timestamp) {
        try {
            // 1. Check if the timestamp is recent (within 60 seconds) to prevent replay
            // attacks
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - requestTime) > 60000) {
                log.warn("Admin signature expired. System time: {}, Request time: {}", currentTime, requestTime);
                return false;
            }

            // 2. Re-calculate the HMAC-SHA256 signature
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(adminSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(timestamp.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);

            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}