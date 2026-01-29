package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_cycles")
@Getter
@Setter
@NoArgsConstructor
public class UsageCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cycle_start", nullable = false)
    private Instant cycleStart;

    @Column(name = "cycle_end", nullable = false)
    private Instant cycleEnd;

    @Column(name = "notes_used", nullable = false)
    private Integer notesUsed = 0;
}
