package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.UsageCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageCycleRepository extends JpaRepository<UsageCycle, UUID> {
    Optional<UsageCycle> findByUserIdAndCycleStart(UUID userId, Instant cycleStart);

    @Modifying
    @Query("UPDATE UsageCycle u SET u.notesUsed = u.notesUsed + 1 WHERE u.id = :cycleId AND u.notesUsed < :limit")
    int incrementUsage(@Param("cycleId") UUID cycleId, @Param("limit") int limit);

    @Modifying
    @Query("UPDATE UsageCycle u SET u.notesUsed = u.notesUsed - 1 WHERE u.id = :cycleId AND u.notesUsed > 0")
    int decrementUsage(@Param("cycleId") UUID cycleId);
}
