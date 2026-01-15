package com.project_x.project_x_backend.controller;

import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackRes;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.service.NoteService;
import com.project_x.project_x_backend.service.AuthService;

import jakarta.validation.Valid;

import com.project_x.project_x_backend.dto.NoteDTO.NoteFilter;
import com.project_x.project_x_backend.dto.NoteDTO.NoteRes;
import com.project_x.project_x_backend.dto.NoteDTO.NoteUploadResponse;
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
@RequestMapping("/api/v1/notes")
public class NoteController {
    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    // TODO: move this config and lessen supported types
    private static final List<String> SUPPORTED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/wav", "audio/x-wav", "audio/mp4",
            "audio/x-m4a", "audio/flac", "audio/wave");

    @Autowired
    private NoteService noteService;

    @Autowired
    private AuthService authService;

    // TODO: check audio length and suspend if more than max allowed
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoteUploadResponse> uploadAudioFile(
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
            NoteUploadResponse noteUploadResponse = noteService.uploadNote(userId, audioBytes, contentType, false);
            return ResponseEntity.status(HttpStatus.CREATED).body(noteUploadResponse);
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

    // for now filter supports -> tag_id, a text matching to note's stt
    // TODO: support pagination
    @PostMapping("/query")
    public ResponseEntity<List<NoteRes>> getNotesWithFilter(@RequestHeader("Authorization") String authorization,
            @RequestBody NoteFilter filter) {
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(noteService.getNotes(userId, filter));
        } catch (Exception e) {
            logger.error("Error getting notes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNote(@RequestHeader("Authorization") String authorization,
            @PathVariable("id") UUID noteId) {
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            noteService.deleteNote(userId, noteId);
            return ResponseEntity.ok("Note deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting note: {}", e.getMessage());
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
            boolean jobCompleted = noteService.handleEngineCallback(request);
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