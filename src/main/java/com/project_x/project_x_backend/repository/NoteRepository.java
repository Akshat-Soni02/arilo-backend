package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.enums.NoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID>, JpaSpecificationExecutor<Note> {

    List<Note> findByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.deletedAt IS NULL")
    List<Note> findActiveNotesByUserId(@Param("userId") UUID userId);

    @Query("SELECT n FROM Note n WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<Note> findActiveNoteById(@Param("id") UUID id);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.status = :status AND n.deletedAt IS NULL")
    List<Note> findByUserIdAndStatus(@Param("userId") UUID userId,
            @Param("status") NoteStatus status);

    @Query("SELECT COUNT(n) FROM Note n WHERE n.userId = :userId AND n.deletedAt IS NULL")
    Long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(n.durationSeconds) FROM Note n WHERE n.userId = :userId AND n.deletedAt IS NULL")
    Double sumDurationByUserId(@Param("userId") UUID userId);

}
