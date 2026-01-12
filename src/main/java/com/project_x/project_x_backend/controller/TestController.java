package com.project_x.project_x_backend.controller;

import com.project_x.project_x_backend.dao.ExtractedTaskDAO;
import com.project_x.project_x_backend.dao.JobDAO;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.CreateTask;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.test.TestCreateTask;
import com.project_x.project_x_backend.dto.NoteDTO.NoteUploadResponse;
import com.project_x.project_x_backend.dto.jobDTO.test.TestCreateJob;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.service.NoteService;
import com.project_x.project_x_backend.service.AuthService;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

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
        return "test";
    }

    @PostMapping("/task")
    public ResponseEntity<CreateTask> createTask(@RequestBody TestCreateTask testCreateTask) {
        CreateTask createTask = new CreateTask(
                testCreateTask.getJobId(),
                testCreateTask.getUserId(),
                testCreateTask.getTask(),
                testCreateTask.getStatus());
        extractedTaskDAO.createExtractedTask(createTask);
        return ResponseEntity.ok(createTask);
    }

    @PostMapping("/job")
    public Job createJob(@RequestBody TestCreateJob testCreateJob) {
        return jobDAO.createMockJob(testCreateJob);
    }

    @PostMapping("/note")
    public ResponseEntity<NoteUploadResponse> createNote(@RequestHeader("Authorization") String authorization) {
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            NoteUploadResponse noteUploadResponse = audioService.uploadNote(userId, new byte[0], "", true);
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
}
