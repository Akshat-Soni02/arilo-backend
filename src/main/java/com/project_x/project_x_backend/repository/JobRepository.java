package com.project_x.project_x_backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project_x.project_x_backend.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
}
