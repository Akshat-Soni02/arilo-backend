package com.project_x.project_x_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project_x.project_x_backend.dao.ExtractedTaskDAO;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskReq;
import com.project_x.project_x_backend.entity.ExtractedTask;

@Service
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
        extractedTaskDAO.deleteTask(id);
    }

}
