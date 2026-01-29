package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.UserDailyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDailyUsageRepository extends JpaRepository<UserDailyUsage, UUID> {
    Optional<UserDailyUsage> findByUserIdAndUsageDate(UUID userId, LocalDate usageDate);

    @Modifying
    @Query(value = """
            INSERT INTO user_daily_usages (user_id, usage_date, notes_used)
            VALUES (:userId, :date, 0)
            ON CONFLICT (user_id, usage_date) DO NOTHING
            """, nativeQuery = true)
    void ensureDailyUsageRow(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date);

    @Modifying
    @Query("""
                UPDATE UserDailyUsage u
                SET u.notesUsed = u.notesUsed + 1
                WHERE u.user.id = :userId
                AND u.usageDate = :date
                AND u.notesUsed < :limit
            """)
    int incrementUsage(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date,
            @Param("limit") int limit);

    @Modifying
    @Query("""
            UPDATE UserDailyUsage u
            SET u.notesUsed = u.notesUsed - 1
            WHERE u.user.id = :userId
              AND u.usageDate = :date
              AND u.notesUsed > 0
            """)
    int decrementUsage(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date);
}
