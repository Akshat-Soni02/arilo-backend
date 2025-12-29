package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.AudioStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AudioRepository extends JpaRepository<AudioStore, UUID> {

    List<AudioStore> findByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("SELECT a FROM AudioStore a WHERE a.userId = :userId AND a.deletedAt IS NULL")
    List<AudioStore> findActiveAudioByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM AudioStore a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<AudioStore> findActiveAudioById(@Param("id") UUID id);

    @Query("SELECT a FROM AudioStore a WHERE a.userId = :userId AND a.status = :status AND a.deletedAt IS NULL")
    List<AudioStore> findByUserIdAndStatus(@Param("userId") UUID userId,
            @Param("status") AudioStore.Status status);

    @Query("SELECT COUNT(a) FROM AudioStore a WHERE a.userId = :userId AND a.deletedAt IS NULL")
    Long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(a.durationSeconds) FROM AudioStore a WHERE a.userId = :userId AND a.deletedAt IS NULL")
    Double sumDurationByUserId(@Param("userId") UUID userId);

}
