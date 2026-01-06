package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import com.project_x.project_x_backend.entity.SmartNote;

@Repository
public interface SmartNoteRepository extends JpaRepository<SmartNote, UUID> {

}
