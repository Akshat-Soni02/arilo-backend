package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.project_x.project_x_backend.enums.TagSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "name" })
})
@Data
public class Tag {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_by", nullable = false)
    private TagSource createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NoteTag> noteTags;
}
