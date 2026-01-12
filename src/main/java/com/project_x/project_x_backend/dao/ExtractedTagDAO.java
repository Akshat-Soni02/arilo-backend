package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

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
        createExtractedTag(createTag);
    }

    public void deleteExtractedTags(UUID noteId) {
        List<ExtractedTag> extractedTags = extractedTagRepository.findAllByNoteId(noteId);
        for (ExtractedTag extractedTag : extractedTags) {
            extractedTagRepository.delete(extractedTag);
        }
    }
}
