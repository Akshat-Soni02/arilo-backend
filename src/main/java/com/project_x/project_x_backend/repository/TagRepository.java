package com.project_x.project_x_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.entity.User;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByUserIdAndName(UUID userId, String name);

    List<Tag> findByUser(User user);
}
