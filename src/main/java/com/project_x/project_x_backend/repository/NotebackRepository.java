package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import com.project_x.project_x_backend.entity.Noteback;

@Repository
public interface NotebackRepository extends JpaRepository<Noteback, UUID> {
    List<Noteback> findAllByNoteId(UUID noteId);
}
