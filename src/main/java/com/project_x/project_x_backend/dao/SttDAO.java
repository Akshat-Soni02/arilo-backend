package com.project_x.project_x_backend.dao;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.SttDTO.CreateStt;
import com.project_x.project_x_backend.entity.Stt;
import com.project_x.project_x_backend.repository.NoteRepository;
import com.project_x.project_x_backend.repository.SttRepository;
import com.project_x.project_x_backend.repository.JobRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class SttDAO {

    @Autowired
    private SttRepository sttRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NoteRepository noteRepository;

    public void createStt(CreateStt createStt) {
        Stt stt = new Stt();
        stt.setUser(userRepository.getReferenceById(createStt.getUserId()));
        com.project_x.project_x_backend.entity.Job job = jobRepository.getReferenceById(createStt.getJobId());
        stt.setJob(job);
        stt.setNote(noteRepository.getReferenceById(job.getNoteId()));
        stt.setLanguage(createStt.getLanguage());
        stt.setStt(createStt.getStt());
        stt.setCreatedAt(Instant.now());
        stt.setUpdatedAt(Instant.now());
        sttRepository.save(stt);
    }
}
