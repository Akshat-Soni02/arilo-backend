package com.project_x.project_x_backend.controller;

import com.project_x.project_x_backend.dto.AudioUploadResponse;
import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackRes;
import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.service.AudioService;
import com.project_x.project_x_backend.service.AuthService;

import jakarta.validation.Valid;

import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackReq;
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
public class AudioController {
    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);

    private static final List<String> SUPPORTED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/wav", "audio/x-wav", "audio/mp4",
            "audio/x-m4a", "audio/flac", "audio/wave");

    @Autowired
    private AudioService audioService;

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioUploadResponse> uploadAudioFile(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("file") MultipartFile file) {

        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String contentType = file.getContentType();
            if (contentType == null || !SUPPORTED_AUDIO_TYPES.contains(contentType)) {
                logger.warn("Unsupported content type: {}", contentType);
                return ResponseEntity.badRequest().build();
            }

            byte[] audioBytes = file.getBytes();
            Note note = audioService.uploadAudio(userId, audioBytes, contentType);
            AudioUploadResponse response = new AudioUploadResponse(note);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            logger.error("IO error during audio upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during audio upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/engine/callback")
    public ResponseEntity<EngineCallbackRes> engineCallback(@RequestBody @Valid EngineCallbackReq request) {
        // if req status is failed we check all the stages and if all of them are failed
        // then only we mark the job failed
        // similiarly if all the stages are completed then we mark job completed
        // also if the stage is completed, we update the output in the db

        try {
            boolean jobCompleted = audioService.handleEngineCallback(request);
            if (!jobCompleted)
                logger.warn("Job failed by engine");
            return ResponseEntity.ok(new EngineCallbackRes("ok"));
        } catch (Exception e) {
            logger.error("Error in engine callback: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(
            @RequestHeader("Authorization") String authorization) {
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT validation failed");
            }
            return ResponseEntity.ok("Authentication successful for user: " + userId);
        } catch (Exception e) {
            logger.error("Auth test error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        logger.error("File size exceeded: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File size exceeds maximum allowed size");
    }
}