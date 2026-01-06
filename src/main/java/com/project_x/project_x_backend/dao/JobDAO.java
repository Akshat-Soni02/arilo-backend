package com.project_x.project_x_backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.project_x.project_x_backend.dto.jobDTO.CreateJob;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.enums.JobStatus;
import com.project_x.project_x_backend.repository.JobRepository;

@Repository
public class JobDAO {
    @Autowired
    private JobRepository jobRepository;

    public Job createJob(CreateJob job) {
        Job jobEntity = new Job();
        jobEntity.setUserId(job.getUserId());
        jobEntity.setAudioId(job.getAudioId());
        jobEntity.setStatus(JobStatus.PROCESSING);
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
