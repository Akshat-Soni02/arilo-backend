package com.project_x.project_x_backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.jobDTO.CreateJob;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.repository.JobRepository;

@Component
public class JobDAO {
    @Autowired
    private JobRepository jobRepository;

    public Job createJob(CreateJob job) {
        Job jobEntity = new Job();
        jobEntity.setUserId(job.getUserId());
        jobEntity.setNoteId(job.getNoteId());
        jobEntity.setStatus(JobStatus.PROCESSING);
        return jobRepository.save(jobEntity);
    }

    public Job createMockJob(com.project_x.project_x_backend.dto.jobDTO.test.TestCreateJob testCreateJob) {
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
        }
    }

    public void markJobFailed(UUID id, String errorMessage) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            jobRepository.save(job);
        }
    }

    public void markJobCompleted(UUID id) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job != null) {
            job.setStatus(JobStatus.COMPLETED);
            jobRepository.save(job);
        }
    }

    public void deleteJob(Job job) {
        jobRepository.delete(job);
    }

    public Job getJobById(UUID id) {
        return jobRepository.findById(id).orElse(null);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

}
