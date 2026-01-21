package com.project_x.project_x_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskReq;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskRes;
import com.project_x.project_x_backend.entity.ExtractedTask;
import com.project_x.project_x_backend.service.AuthService;
import com.project_x.project_x_backend.service.TaskService;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuthService authService;

    @GetMapping("")
    public ResponseEntity<List<UpdateTaskRes>> getAllTasks(@RequestHeader("Authorization") String authorization) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<UpdateTaskRes> updateTaskRes = taskService.getAllTasks(userId).stream()
                .map(task -> new UpdateTaskRes(task.getId(), task.getTask(), task.getStatus(), task.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(updateTaskRes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateTaskRes> updateTask(@RequestHeader("Authorization") String authorization,
            @PathVariable UUID id, @RequestBody UpdateTaskReq updateTaskReq) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ExtractedTask task = taskService.updateTask(id, updateTaskReq);
        return ResponseEntity
                .ok(new UpdateTaskRes(task.getId(), task.getTask(), task.getStatus(), task.getCreatedAt()));
    }

    @PostMapping("/delete/all")
    public ResponseEntity<String> deleteAllTasks(@RequestHeader("Authorization") String authorization) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        taskService.deleteAllTasks(userId);
        return ResponseEntity.ok("All tasks deleted successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@RequestHeader("Authorization") String authorization,
            @PathVariable UUID id) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

}
