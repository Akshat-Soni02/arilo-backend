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
import com.project_x.project_x_backend.repository.TagRepository;
import com.project_x.project_x_backend.repository.NoteTagRepository;
import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.enums.TagSource;
import com.project_x.project_x_backend.entity.NoteTag;
import java.util.Optional;
import com.project_x.project_x_backend.entity.Job;

@Component
@org.springframework.transaction.annotation.Transactional
public class ExtractedTagDAO {

    @Autowired
    private ExtractedTagRepository extractedTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NoteTagRepository noteTagRepository;

    public void createExtractedTag(CreateTag createTag, Tag canonicalTag) {
        ExtractedTag extractedTag = new ExtractedTag();
        extractedTag.setUser(userRepository.getReferenceById(createTag.getUserId()));
        Job job = jobRepository.getReferenceById(createTag.getJobId());
        extractedTag.setJob(job);
        extractedTag.setNote(noteRepository.getReferenceById(job.getNoteId()));
        extractedTag.setTag(createTag.getTag());
        extractedTag.setCanonicalTag(canonicalTag);
        extractedTag.setCreatedAt(Instant.now());
        extractedTag = extractedTagRepository.save(extractedTag);

        // If canonical exists, link to note directly
        if (canonicalTag != null) {
            NoteTag noteTag = new NoteTag(extractedTag.getNote(), canonicalTag);
            noteTagRepository.save(noteTag);
        }
    }

    public void addExtractedTag(CreateTag createTag) {
        // Check if canonical tag exists
        Optional<Tag> existingTag = tagRepository.findByUserIdAndName(createTag.getUserId(), createTag.getTag());

        if (existingTag.isPresent()) {
            // Case A: Canonical tag exists -> Link immediately
            createExtractedTag(createTag, existingTag.get());
        } else {
            // Case B: No canonical tag -> Insert raw
            createExtractedTag(createTag, null);

            // Check for promotion
            long count = extractedTagRepository.countByUserIdAndTagAndCanonicalTagIsNull(createTag.getUserId(),
                    createTag.getTag());

            if (count >= 3) {
                promoteTag(createTag.getUserId(), createTag.getTag());
            }
        }
    }

    private void promoteTag(UUID userId, String tagName) {
        // Create canonical tag
        Tag newTag = new Tag();
        newTag.setId(UUID.randomUUID());
        newTag.setUser(userRepository.getReferenceById(userId));
        newTag.setName(tagName);
        newTag.setCreatedBy(TagSource.AI_EXTRACTED);
        newTag.setCreatedAt(Instant.now());
        newTag.setUpdatedAt(Instant.now());
        newTag = tagRepository.save(newTag);

        // Update all matching extracted_tags
        List<ExtractedTag> rawTags = extractedTagRepository.findByUserIdAndTagAndCanonicalTagIsNull(userId, tagName);
        for (ExtractedTag rawTag : rawTags) {
            rawTag.setCanonicalTag(newTag);
            extractedTagRepository.save(rawTag);

            // Create NoteTags
            NoteTag noteTag = new NoteTag(rawTag.getNote(), newTag);
            noteTagRepository.save(noteTag);
        }
    }

    public void deleteExtractedTags(UUID noteId) {
        List<ExtractedTag> extractedTags = extractedTagRepository.findAllByNoteId(noteId);
        for (ExtractedTag extractedTag : extractedTags) {
            extractedTagRepository.delete(extractedTag);
        }
    }
}
