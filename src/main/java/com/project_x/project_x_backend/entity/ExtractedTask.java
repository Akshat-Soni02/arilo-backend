package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.project_x.project_x_backend.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "extracted_tasks")
@Data
public class ExtractedTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @Column(columnDefinition = "task", nullable = false)
    private String task;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
