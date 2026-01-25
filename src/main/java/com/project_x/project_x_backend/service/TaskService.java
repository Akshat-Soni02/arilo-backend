package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.dao.ExtractedTaskDAO;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskReq;
import com.project_x.project_x_backend.entity.ExtractedTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private ExtractedTaskDAO extractedTaskDAO;

    public List<ExtractedTask> getAllTasks(UUID userId) {
        return extractedTaskDAO.getAllUserTasks(userId);
    }

    public ExtractedTask updateTask(UUID id, UpdateTaskReq updateTaskReq) {
        return extractedTaskDAO.updateTask(id, updateTaskReq);
    }

    public void deleteTask(UUID id) {
        log.info("Deleting extracted task ID: {}", id);
        try {
            extractedTaskDAO.deleteTask(id);
            log.info("Successfully deleted task ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete task {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public void deleteAllTasks(UUID userId) {
        log.info("Deleting all extracted tasks for user {}", userId);
        try {
            extractedTaskDAO.deleteAllUserTasks(userId);
            log.info("Successfully deleted all tasks for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete all tasks for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
