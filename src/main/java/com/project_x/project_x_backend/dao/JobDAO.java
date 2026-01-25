package com.project_x.project_x_backend.dao;

import com.project_x.project_x_backend.dto.jobDTO.CreateJob;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JobDAO {
    @Autowired
    private JobRepository jobRepository;

    public Job getJobById(UUID id) {
        log.debug("Fetching job by ID: {}", id);
        return jobRepository.findById(id).orElse(null);
    }

    public Job createJob(CreateJob job) {
        log.info("Creating new job for user {} and note {}", job.getUserId(), job.getNoteId());
        try {
            Job jobEntity = new Job();
            jobEntity.setUserId(job.getUserId());
            jobEntity.setNoteId(job.getNoteId());
            jobEntity.setStatus(JobStatus.PROCESSING);
            Job saved = jobRepository.save(jobEntity);
            log.info("Successfully created job ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create job for user {}: {}", job.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    public Job createMockJob(com.project_x.project_x_backend.dto.jobDTO.test.TestCreateJob testCreateJob) {
        log.info("Creating mock job for user {} and note {}", testCreateJob.getUserId(), testCreateJob.getNoteId());
        Job jobEntity = new Job();
        jobEntity.setUserId(testCreateJob.getUserId());
        jobEntity.setNoteId(testCreateJob.getNoteId());
        jobEntity.setStatus(testCreateJob.getStatus());
        return jobRepository.save(jobEntity);
    }

    public void updateJobStatus(UUID id, JobStatus status) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null) {
            job.setStatus(status);
            jobRepository.save(job);
            log.debug("Successfully updated job {} status", id);
        } else {
            log.warn("Attempted to update status of non-existent job {}", id);
        }
    }

    public void markJobFailed(UUID id, String errorMessage) {
        log.warn("Marking job {} as FAILED. Error: {}", id, errorMessage);
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            jobRepository.save(job);
        } else {
            log.warn("Attempted to mark non-existent job {} as failed", id);
        }
    }

    public void markJobCompleted(UUID id) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.COMPLETED);
            jobRepository.save(job);
        } else {
            log.warn("Attempted to mark non-existent job {} as completed", id);
        }
    }

    public void deleteJob(Job job) {
        log.info("Deleting job ID: {}", job.getId());
        jobRepository.delete(job);
    }

    public Job getUserJobById(UUID id, UUID userId) {
        log.debug("Fetching job {} for user {}", id, userId);
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null && job.getUserId().equals(userId)) {
            return job;
        }
        return null;
    }

    public Job getJobByNote(Note note) {
        log.debug("Fetching job for note {}", note.getId());
        return jobRepository.findByNoteId(note.getId());
    }

    public List<Job> getAllJobs(UUID userId) {
        log.debug("Fetching all jobs for user {}", userId);
        return jobRepository.findAllByUserId(userId);
    }
}
