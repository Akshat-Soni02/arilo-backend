package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

import com.project_x.project_x_backend.entity.ExtractedTask;
import com.project_x.project_x_backend.entity.User;

@Repository
public interface ExtractedTaskRepository extends JpaRepository<ExtractedTask, UUID> {
    List<ExtractedTask> findByUser(User user);

    List<ExtractedTask> findAllByNoteId(UUID noteId);
}
