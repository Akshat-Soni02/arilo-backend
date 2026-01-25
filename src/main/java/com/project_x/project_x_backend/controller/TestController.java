package com.project_x.project_x_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project_x.project_x_backend.dao.ExtractedTaskDAO;
import com.project_x.project_x_backend.dao.JobDAO;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.CreateTask;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.test.TestCreateTask;
import com.project_x.project_x_backend.dto.NoteDTO.NoteUploadResponse;
import com.project_x.project_x_backend.dto.jobDTO.test.TestCreateJob;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.service.NoteService;
import com.project_x.project_x_backend.service.AuthService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {

    @Autowired
    private ExtractedTaskDAO extractedTaskDAO;

    @Autowired
    private JobDAO jobDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private NoteService audioService;

    @GetMapping("")
    public String test() {
        log.info("Test endpoint called");
        return "test";
    }

    @PostMapping("/task")
    public ResponseEntity<CreateTask> createTask(@RequestBody TestCreateTask testCreateTask) {
        log.info("Test creating task: {}", testCreateTask.getTask());
        try {
            CreateTask createTask = new CreateTask(
                    testCreateTask.getJobId(),
                    testCreateTask.getUserId(),
                    testCreateTask.getTask(),
                    testCreateTask.getStatus());
            extractedTaskDAO.createExtractedTask(createTask);
            log.info("Successfully created test task for job {}", testCreateTask.getJobId());
            return ResponseEntity.ok(createTask);
        } catch (Exception e) {
            log.error("Error creating test task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/job")
    public ResponseEntity<Job> createJob(@RequestBody TestCreateJob testCreateJob) {
        log.info("Test creating mock job for note {}", testCreateJob.getNoteId());
        try {
            Job job = jobDAO.createMockJob(testCreateJob);
            log.info("Successfully created test job {}", job.getId());
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            log.error("Error creating test job: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/note")
    public ResponseEntity<NoteUploadResponse> createNote(@RequestHeader("Authorization") String authorization) {
        log.info("Test creating mock note");
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized test note creation attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            NoteUploadResponse noteUploadResponse = audioService.uploadNote(userId, new byte[0], "", true);
            log.info("Successfully created test note ID {} for user {}", noteUploadResponse.getNoteId(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(noteUploadResponse);
        } catch (IOException e) {
            log.error("IO error during test audio upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error during test audio upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during test audio upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
