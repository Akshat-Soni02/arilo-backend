package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.NotebackDTO.CreateNoteback;
import com.project_x.project_x_backend.entity.Job;
import com.project_x.project_x_backend.entity.Noteback;
import com.project_x.project_x_backend.repository.NoteRepository;
import com.project_x.project_x_backend.repository.NotebackRepository;
import com.project_x.project_x_backend.repository.JobRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class NotebackDAO {

    @Autowired
    private NotebackRepository notebackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NoteRepository noteRepository;

    public void createNoteback(CreateNoteback createNoteback) {
        Noteback noteback = new Noteback();
        noteback.setUser(userRepository.getReferenceById(createNoteback.getUserId()));
        com.project_x.project_x_backend.entity.Job job = jobRepository.getReferenceById(createNoteback.getJobId());
        noteback.setJob(job);
        noteback.setNote(noteRepository.getReferenceById(job.getNoteId()));
        noteback.setNoteContent(createNoteback.getNote());
        noteback.setMetadata(createNoteback.getMetadata());
        noteback.setCreatedAt(Instant.now());
        notebackRepository.save(noteback);
    }

    public void deleteExtractedNotebacks(UUID noteId) {
        List<Noteback> notebacks = notebackRepository.findAllByNoteId(noteId);
        for (Noteback noteback : notebacks) {
            notebackRepository.delete(noteback);
        }
    }

    public Optional<Noteback> getNotebackByJob(Job job) {
        return notebackRepository.findByJob(job);
    }
}
