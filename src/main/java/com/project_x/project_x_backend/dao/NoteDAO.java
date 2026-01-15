package com.project_x.project_x_backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.entity.Note;
import com.project_x.project_x_backend.repository.NoteRepository;
import com.project_x.project_x_backend.dao.NoteTagDAO;

@Component
public class NoteDAO {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ExtractedTagDAO extractedTagDAO;

    @Autowired
    private ExtractedTaskDAO extractedTaskDAO;

    @Autowired
    private AnxietyScoreDAO anxietyScoreDAO;

    @Autowired
    private SttDAO sttDAO;

    @Autowired
    private NotebackDAO notebackDAO;

    @Autowired
    private NoteTagDAO noteTagDAO;

    public List<Note> findUserNotes(UUID userId) {
        return noteRepository.findActiveNotesByUserId(userId);
    }

    // if a note's job exists, it will stay and we don't touch it
    public void deleteNote(UUID userId, UUID noteId) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("User is not authorized to delete this note");
        }

        // deleting extracted properties
        deleteExtractedProperties(noteId);

        // deleting note tags
        noteTagDAO.deleteNoteTags(noteId);

        // deleting note
        noteRepository.delete(note);
    }

    public void deleteExtractedProperties(UUID noteId) {
        extractedTagDAO.deleteExtractedTags(noteId);
        extractedTaskDAO.deleteExtractedTasks(noteId);
        anxietyScoreDAO.deleteExtractedAnxietyScores(noteId);
        sttDAO.deleteExtractedStt(noteId);
        notebackDAO.deleteExtractedNotebacks(noteId);
    }
}
