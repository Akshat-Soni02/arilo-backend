package com.project_x.project_x_backend.dao;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.ExtractedTaskDTO.CreateTask;
import com.project_x.project_x_backend.entity.ExtractedTask;
import com.project_x.project_x_backend.repository.ExtractedTaskRepository;
import com.project_x.project_x_backend.repository.JobRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class ExtractedTaskDAO {

    @Autowired
    private ExtractedTaskRepository extractedTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    public void createExtractedTask(CreateTask createTask) {
        ExtractedTask extractedTask = new ExtractedTask();
        extractedTask.setUser(userRepository.getReferenceById(createTask.getUserId()));
        extractedTask.setJob(jobRepository.getReferenceById(createTask.getJobId()));
        extractedTask.setTask(createTask.getTask());
        extractedTask.setCreatedAt(Instant.now());
        extractedTask.setUpdatedAt(Instant.now());
        extractedTaskRepository.save(extractedTask);
    }
}
