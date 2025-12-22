package com.project_x.project_x_backend.controller;

import com.project_x.project_x_backend.dto.AudioUploadResponse;
import com.project_x.project_x_backend.entity.AudioStore;
import com.project_x.project_x_backend.service.AudioService;
import com.project_x.project_x_backend.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin(origins = { "http://localhost:5173" })
public class AudioController {

    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);

    @Autowired
    private AudioService audioService;

    @Autowired
    private JwtService jwtService;

    // List of supported audio MIME types
    private static final List<String> SUPPORTED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg",
            "audio/wav",
            "audio/x-wav",
            "audio/mp4",
            "audio/x-m4a",
            "audio/flac");

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioUploadResponse> uploadAudioFile(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file) {

        logger.info("=== Audio Upload Request Received ===");
        logger.info("Authorization header present: {}", authorization != null && !authorization.isEmpty());
        logger.info("File present: {}", file != null);
        logger.info("File name: {}", file != null ? file.getOriginalFilename() : "null");
        logger.info("File size: {} bytes", file != null ? file.getSize() : 0);
        logger.info("File content type: {}", file != null ? file.getContentType() : "null");

        try {

            // Extract and validate JWT token
            logger.info("Extracting user ID from JWT token...");
            UUID userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                logger.warn("JWT token validation failed - returning 401 Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            logger.info("JWT token validated successfully for user: {}", userId);

            // Validate file
            if (file.isEmpty()) {
                logger.warn("File is empty - returning 400 Bad Request");
                return ResponseEntity.badRequest().build();
            }

            // Validate content type
            String contentType = file.getContentType();
            if (contentType == null || !SUPPORTED_AUDIO_TYPES.contains(contentType)) {
                logger.warn("Unsupported content type: {} - returning 400 Bad Request", contentType);
                logger.info("Supported types: {}", SUPPORTED_AUDIO_TYPES);
                return ResponseEntity.badRequest().build();
            }
            logger.info("Content type validation passed: {}", contentType);

            // Get file bytes
            logger.info("Converting file to byte array...");
            byte[] audioBytes = file.getBytes();
            logger.info("File converted to byte array successfully. Size: {} bytes", audioBytes.length);

            // Upload audio bytes
            logger.info("Calling AudioService.uploadAudio()...");
            AudioStore audioStore = audioService.uploadAudio(userId, audioBytes, contentType);
            logger.info("AudioService.uploadAudio() completed successfully");
            logger.info("AudioStore ID: {}", audioStore.getId());
            logger.info("GCS URL: {}", audioStore.getGcsAudioUrl());

            // Create response
            AudioUploadResponse response = new AudioUploadResponse(audioStore);
            logger.info("=== Audio Upload Successful ===");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            logger.error("IOException during audio upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException during audio upload: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected exception during audio upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Test endpoint to debug JWT authentication
     */
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(
            @RequestHeader("Authorization") String authorization) {

        logger.info("=== Auth Test Request Received ===");
        logger.info("Authorization header: {}", authorization);

        try {
            UUID userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                logger.warn("JWT validation failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("JWT validation failed");
            }

            logger.info("JWT validation successful for user: {}", userId);
            return ResponseEntity.ok("Authentication successful for user: " + userId);

        } catch (Exception e) {
            logger.error("Auth test error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Handle file size exceeded exception
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        logger.error("File size exceeded maximum allowed size: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File size exceeds maximum allowed size");
    }

    private UUID extractUserIdFromToken(String authorization) {
        logger.info("=== JWT Token Extraction Debug ===");
        logger.info("Authorization header received: '{}'", authorization);

        try {
            if (authorization == null) {
                logger.warn("Authorization header is null");
                return null;
            }

            if (!authorization.startsWith("Bearer ")) {
                logger.warn("Authorization header does not start with 'Bearer '. Actual start: '{}'",
                        authorization.length() > 10 ? authorization.substring(0, 10) + "..." : authorization);
                return null;
            }

            String token = authorization.substring(7);
            logger.info("Extracted token (first 20 chars): '{}'",
                    token.length() > 20 ? token.substring(0, 20) + "..." : token);
            logger.info("Token length: {}", token.length());

            logger.info("Validating token with JWT service...");
            boolean isValid = jwtService.isTokenValid(token);
            logger.info("Token validation result: {}", isValid);

            if (!isValid) {
                logger.warn("JWT token validation failed");
                return null;
            }

            logger.info("Extracting user ID from token...");
            UUID userId = jwtService.extractUserId(token);
            logger.info("User ID extracted successfully: {}", userId);
            return userId;

        } catch (Exception e) {
            logger.error("Error extracting user ID from token: {}", e.getMessage(), e);
            return null;
        }
    }

}