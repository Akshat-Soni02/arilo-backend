package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.ExtractedTagDTO.CreateTag;
import com.project_x.project_x_backend.entity.ExtractedTag;
import com.project_x.project_x_backend.repository.NoteRepository;
import com.project_x.project_x_backend.repository.ExtractedTagRepository;
import com.project_x.project_x_backend.repository.JobRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class ExtractedTagDAO {

    @Autowired
    private ExtractedTagRepository extractedTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NoteRepository noteRepository;

    public void createExtractedTag(CreateTag createTag) {
        ExtractedTag extractedTag = new ExtractedTag();
        extractedTag.setUser(userRepository.getReferenceById(createTag.getUserId()));
        com.project_x.project_x_backend.entity.Job job = jobRepository.getReferenceById(createTag.getJobId());
        extractedTag.setJob(job);
        extractedTag.setNote(noteRepository.getReferenceById(job.getNoteId()));
        extractedTag.setTag(createTag.getTag());
        extractedTag.setCreatedAt(Instant.now());
        extractedTagRepository.save(extractedTag);
    }

    public void addExtractedTag(CreateTag createTag) {
        Optional<ExtractedTag> existingTag = extractedTagRepository.findByTag(createTag.getTag());
        if (existingTag.isPresent()) {
            existingTag.get().setTagCount(existingTag.get().getTagCount() + createTag.getTagCount());
            extractedTagRepository.save(existingTag.get());
        } else {
            createExtractedTag(createTag);
        }
    }
}
