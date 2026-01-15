package com.project_x.project_x_backend.dto.NoteDTO;

import java.time.Instant;
import java.util.UUID;

import com.project_x.project_x_backend.enums.NoteSortField;
import com.project_x.project_x_backend.enums.SortOrder;

import lombok.Data;

@Data
public class NoteFilter {
    private UUID tagId;
    private String q;
    private Instant createdAfter;
    private Instant createdBefore;
    private NoteSortField sort;
    private SortOrder order;
}
