package com.project_x.project_x_backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@Slf4j
public class GoogleTokenService {

    @Value("${app.google.client-id}")
    private String googleClientId;

    public GoogleIdToken.Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
        log.debug("Verifying Google ID token with client ID: {}", googleClientId);
        
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                log.info("Google ID token verified successfully for: {}", idToken.getPayload().getEmail());
                return idToken.getPayload();
            } else {
                log.error("Google ID token verification failed: verifier returned null");
                throw new IllegalArgumentException("Invalid ID token.");
            }
        } catch (Exception e) {
            log.error("Exception during Google ID token verification: {}", e.getMessage(), e);
            throw e;
        }
    }
}
