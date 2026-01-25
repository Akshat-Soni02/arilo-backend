package com.project_x.project_x_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskReq;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskRes;
import com.project_x.project_x_backend.entity.ExtractedTask;
import com.project_x.project_x_backend.service.AuthService;
import com.project_x.project_x_backend.service.TaskService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuthService authService;

    @GetMapping("")
    public ResponseEntity<List<UpdateTaskRes>> getAllTasks(@RequestHeader("Authorization") String authorization) {
        log.info("Received request to get all tasks");
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized attempt to get all tasks");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<UpdateTaskRes> tasks = taskService.getAllTasks(userId).stream()
                    .map(task -> new UpdateTaskRes(task.getId(), task.getTask(), task.getStatus(), task.getCreatedAt()))
                    .collect(Collectors.toList());

            log.info("Returned {} tasks for user {}", tasks.size(), userId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error getting all tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateTaskRes> updateTask(@RequestHeader("Authorization") String authorization,
            @PathVariable UUID id, @RequestBody UpdateTaskReq updateTaskReq) {
        log.info("Received request to update task: {}", id);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized attempt to update task {}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            ExtractedTask task = taskService.updateTask(id, updateTaskReq);
            log.info("Successfully updated task {} for user {}", id, userId);
            return ResponseEntity
                    .ok(new UpdateTaskRes(task.getId(), task.getTask(), task.getStatus(), task.getCreatedAt()));
        } catch (Exception e) {
            log.error("Error updating task {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/delete/all")
    public ResponseEntity<String> deleteAllTasks(@RequestHeader("Authorization") String authorization) {
        log.info("Received request to delete all tasks");
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized attempt to delete all tasks");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            taskService.deleteAllTasks(userId);
            log.info("Successfully deleted all tasks for user {}", userId);
            return ResponseEntity.ok("All tasks deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting all tasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@RequestHeader("Authorization") String authorization,
            @PathVariable UUID id) {
        log.info("Received request to delete task: {}", id);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized attempt to delete task {}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            taskService.deleteTask(id);
            log.info("Successfully deleted task {} for user {}", id, userId);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting task {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
