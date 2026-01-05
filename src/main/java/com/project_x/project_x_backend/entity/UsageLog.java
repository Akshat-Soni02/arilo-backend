package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.sql.Timestamp;

@Entity
@Table(name = "usage_logs")
@Data
public class UsageLog {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "audio_id", nullable = false)
    private AudioStore audio;

    @Column(name = "input_token")
    private Integer inputToken;

    @Column(name = "output_token")
    private Integer outputToken;

    @Column(name = "thought_token")
    private Integer thoughtToken;

    private String model;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
