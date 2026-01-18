package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.NoteSentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NoteSentenceRepository extends JpaRepository<NoteSentence, UUID> {
}
