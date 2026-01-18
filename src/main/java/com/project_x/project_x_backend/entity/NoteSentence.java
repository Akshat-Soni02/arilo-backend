package com.project_x.project_x_backend.entity;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "note_sentences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Column(name = "sentence_index", nullable = false)
    private Integer sentenceIndex;

    @Column(name = "sentence_text", nullable = false, columnDefinition = "TEXT")
    private String sentenceText;

    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;

    @Column(name = "importance_score")
    private Float importanceScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
