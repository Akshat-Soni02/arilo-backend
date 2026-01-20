package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import com.project_x.project_x_backend.entity.ExtractedTag;

@Repository
public interface ExtractedTagRepository extends JpaRepository<ExtractedTag, UUID> {

    long countByUserIdAndTagAndCanonicalTagIsNull(UUID userId, String tag);

    List<ExtractedTag> findByUserIdAndTagAndCanonicalTagIsNull(UUID userId, String tag);

    List<ExtractedTag> findAllByNoteId(UUID noteId);

    List<ExtractedTag> findByUserIdAndCanonicalTagIsNull(UUID userId);

    @Query("SELECT e FROM ExtractedTag e WHERE e.id IN (" +
            "SELECT MIN(e2.id) FROM ExtractedTag e2 " +
            "WHERE e2.user.id = :userId AND e2.canonicalTag IS NULL " +
            "GROUP BY e2.tag)")
    List<ExtractedTag> findUniqueExtractedTagsByUserIdAndCanonicalTagIsNull(@Param("userId") UUID userId);
}
