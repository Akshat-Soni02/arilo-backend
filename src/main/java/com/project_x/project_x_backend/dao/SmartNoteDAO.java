package com.project_x.project_x_backend.dao;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.SmartNoteDTO.CreateSmartNote;
import com.project_x.project_x_backend.entity.SmartNote;
import com.project_x.project_x_backend.repository.SmartNoteRepository;
import com.project_x.project_x_backend.repository.JobRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class SmartNoteDAO {

    @Autowired
    private SmartNoteRepository smartNoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    public void createSmartNote(CreateSmartNote createSmartNote) {
        SmartNote smartNote = new SmartNote();
        smartNote.setUser(userRepository.getReferenceById(createSmartNote.getUserId()));
        smartNote.setJob(jobRepository.getReferenceById(createSmartNote.getJobId()));
        smartNote.setNote(createSmartNote.getNote());
        smartNote.setMetadata(createSmartNote.getMetadata());
        smartNote.setCreatedAt(Instant.now());
        smartNoteRepository.save(smartNote);
    }
}
