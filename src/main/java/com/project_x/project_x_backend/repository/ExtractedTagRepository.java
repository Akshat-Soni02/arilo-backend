package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import com.project_x.project_x_backend.entity.ExtractedTag;

@Repository
public interface ExtractedTagRepository extends JpaRepository<ExtractedTag, UUID> {
    Optional<ExtractedTag> findByTag(String tag);
}
