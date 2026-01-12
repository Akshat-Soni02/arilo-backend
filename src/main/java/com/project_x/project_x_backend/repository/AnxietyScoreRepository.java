package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

import com.project_x.project_x_backend.entity.AnxietyScore;

@Repository
public interface AnxietyScoreRepository extends JpaRepository<AnxietyScore, UUID> {
    List<AnxietyScore> findAllByNoteId(UUID noteId);
}
