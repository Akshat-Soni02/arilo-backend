package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    Optional <User> findById(UUID id);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveUserById(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.googleId = :googleId AND u.deletedAt IS NULL")
    Optional<User> findActiveUserByGoogleId(@Param("googleId") String googleId);

    boolean existsByEmail(String email);
}
