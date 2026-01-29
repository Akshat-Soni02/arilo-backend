package com.project_x.project_x_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_daily_usages", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "usage_date" })
})
@Getter
@Setter
@NoArgsConstructor
public class UserDailyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "notes_used", nullable = false)
    private Integer notesUsed = 0;
}
