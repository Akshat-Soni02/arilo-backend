package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import com.project_x.project_x_backend.entity.ExtractedTag;

@Repository
public interface ExtractedTagRepository extends JpaRepository<ExtractedTag, UUID> {

    long countByUserIdAndTagAndCanonicalTagIsNull(UUID userId, String tag);

    List<ExtractedTag> findByUserIdAndTagAndCanonicalTagIsNull(UUID userId, String tag);

    List<ExtractedTag> findAllByNoteId(UUID noteId);
}
