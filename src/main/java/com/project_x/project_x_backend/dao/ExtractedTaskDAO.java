package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.ExtractedTaskDTO.CreateTask;
import com.project_x.project_x_backend.dto.ExtractedTaskDTO.UpdateTaskReq;
import com.project_x.project_x_backend.entity.ExtractedTask;
import com.project_x.project_x_backend.repository.NoteRepository;
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

    @Autowired
    private NoteRepository noteRepository;

    public void createExtractedTask(CreateTask createTask) {
        ExtractedTask extractedTask = new ExtractedTask();
        extractedTask.setUser(userRepository.getReferenceById(createTask.getUserId()));
        com.project_x.project_x_backend.entity.Job job = jobRepository.getReferenceById(createTask.getJobId());
        extractedTask.setJob(job);
        extractedTask.setNote(noteRepository.getReferenceById(job.getNoteId()));
        extractedTask.setTask(createTask.getTask());
        extractedTask.setCreatedAt(Instant.now());
        extractedTask.setUpdatedAt(Instant.now());
        extractedTask.setStatus(createTask.getStatus());
        extractedTaskRepository.save(extractedTask);
    }

    public List<ExtractedTask> getAllUserTasks(UUID userId) {
        return extractedTaskRepository.findByUser(userRepository.getReferenceById(userId));
    }

    public ExtractedTask updateTask(UUID id, UpdateTaskReq updateTaskReq) {
        ExtractedTask extractedTask = extractedTaskRepository.getReferenceById(id);

        if (updateTaskReq.getTask() != null) {
            extractedTask.setTask(updateTaskReq.getTask());
        }
        if (updateTaskReq.getStatus() != null) {
            extractedTask.setStatus(updateTaskReq.getStatus());
        }

        extractedTask.setUpdatedAt(Instant.now());
        return extractedTaskRepository.save(extractedTask);
    }

    public void deleteTask(UUID id) {
        extractedTaskRepository.deleteById(id);
    }

    public void deleteAllUserTasks(UUID userId) {
        List<ExtractedTask> extractedTasks = extractedTaskRepository
                .findByUser(userRepository.getReferenceById(userId));
        extractedTaskRepository.deleteAll(extractedTasks);
    }

    public void deleteExtractedTasks(UUID noteId) {
        List<ExtractedTask> extractedTasks = extractedTaskRepository.findAllByNoteId(noteId);
        extractedTaskRepository.deleteAll(extractedTasks);
    }
}
