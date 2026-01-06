package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "extracted_tags")
@Data
public class ExtractedTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name="tag", nullable = false)
    private String tag;

    @Column(name = "tag_count", nullable = false)
    private Integer tagCount = 0;

    @Column(name = "created_at")
    private Instant createdAt;
}
