package com.project_x.project_x_backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import com.project_x.project_x_backend.repository.NoteTagRepository;

@Component
public class NoteTagDAO {

    @Autowired
    private NoteTagRepository noteTagRepository;

    public void deleteNoteTags(UUID noteId) {
        noteTagRepository.deleteByNoteId(noteId);
    }
}
