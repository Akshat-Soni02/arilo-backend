package com.project_x.project_x_backend.controller;

import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackRes;
import com.project_x.project_x_backend.dto.jobDTO.JobPollingRes;
import com.project_x.project_x_backend.service.NoteService;
import com.project_x.project_x_backend.service.AuthService;

import jakarta.validation.Valid;

import com.project_x.project_x_backend.dto.NoteDTO.NoteFilter;
import com.project_x.project_x_backend.dto.NoteDTO.NoteRes;
import com.project_x.project_x_backend.dto.NoteDTO.NoteUploadResponse;
import com.project_x.project_x_backend.dto.jobDTO.EngineCallbackReq;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class NoteController {

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

        log.info("Received audio upload request");
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized access attempt: invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (file == null || file.isEmpty()) {
                log.warn("Audio upload failed: file is empty or null");
                return ResponseEntity.badRequest().build();
            }

            String contentType = file.getContentType();
            if (contentType == null || !SUPPORTED_AUDIO_TYPES.contains(contentType)) {
                log.warn("Unsupported content type for user {}: {}", userId, contentType);
                return ResponseEntity.badRequest().build();
            }

            byte[] audioBytes = file.getBytes();
            log.debug("Uploading audio for user {}: {} bytes", userId, audioBytes.length);
            NoteUploadResponse noteUploadResponse = noteService.uploadNote(userId, audioBytes, contentType, false);
            log.info("Audio successfully uploaded for user {}. Note ID: {}", userId, noteUploadResponse.getNoteId());
            return ResponseEntity.status(HttpStatus.CREATED).body(noteUploadResponse);
        } catch (IOException e) {
            log.error("IO error during audio upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error during audio upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during audio upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // for now filter supports -> tag_id, a text matching to note's stt
    // TODO: support pagination
    @PostMapping("/query")
    public ResponseEntity<List<NoteRes>> getNotesWithFilter(@RequestHeader("Authorization") String authorization,
            @RequestBody NoteFilter filter) {
        log.info("Querying notes with filter: {}", filter);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized query attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<NoteRes> notes = noteService.getNotes(userId, filter);
            log.info("Returned {} notes for user {}", notes.size(), userId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting notes with filter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/poll")
    public ResponseEntity<JobPollingRes> getPollingStatus(@RequestHeader("Authorization") String authorization,
            @RequestParam("job_id") UUID jobId) {
        log.info("Polling status for job {}", jobId);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized polling attempt for job {}", jobId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            JobPollingRes res = noteService.getPollingStatus(userId, jobId);
            log.debug("Polling status for user {}, job {}: {}", userId, jobId, res.getStatus());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error getting polling status for job {}: {}", jobId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNote(@RequestHeader("Authorization") String authorization,
            @PathVariable("id") UUID noteId) {
        log.info("Deleting note {}", noteId);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized delete attempt for note {}", noteId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            noteService.deleteNote(userId, noteId);
            log.info("Note {} successfully deleted for user {}", noteId, userId);
            return ResponseEntity.ok("Note deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting note {}: {}", noteId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/engine/callback")
    public ResponseEntity<EngineCallbackRes> engineCallback(@RequestBody @Valid EngineCallbackReq request) {
        // if req status is failed we check all the stages and if all of them are failed
        // then only we mark the job failed
        // similiarly if all the stages are completed then we mark job completed
        // also if the stage is completed, we update the output in the db
        log.info("Received engine callback for job {}", request.getJobId());
        try {
            boolean jobCompleted = noteService.handleEngineCallback(request);
            if (!jobCompleted) {
                log.warn("Job {} marked as failed by engine callback", request.getJobId());
            } else {
                log.info("Engine callback processed successfully for job {}", request.getJobId());
            }
            return ResponseEntity.ok(new EngineCallbackRes("ok"));
        } catch (Exception e) {
            log.error("Error in engine callback for job {}: {}", request.getJobId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(
            @RequestHeader("Authorization") String authorization) {
        log.info("Testing auth");
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Auth test failed: invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT validation failed");
            }
            log.info("Auth test successful for user {}", userId);
            return ResponseEntity.ok("Authentication successful for user: " + userId);
        } catch (Exception e) {
            log.error("Unexpected error in auth test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        log.error("File size exceeded maximum limit: {}", exc.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File size exceeds maximum allowed size");
    }
}