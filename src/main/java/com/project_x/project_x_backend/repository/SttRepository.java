package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.Stt;

@Repository
public interface SttRepository extends JpaRepository<Stt, UUID> {
    List<Stt> findAllByNoteId(UUID noteId);

    Optional<Stt> findByJob(Job job);
}
