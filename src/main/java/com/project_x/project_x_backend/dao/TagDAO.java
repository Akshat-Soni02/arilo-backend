package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.TagDTO.TagCreateInt;
import com.project_x.project_x_backend.dto.TagDTO.TagUpdateInt;
import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.repository.TagRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class TagDAO {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    public Tag createTag(TagCreateInt tagDTO) {
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setUser(userRepository.getReferenceById(tagDTO.getUserId()));
        tag.setName(tagDTO.getName());
        tag.setDescription(tagDTO.getDescription());
        tag.setCreatedBy(tagDTO.getCreatedBy());
        tag.setCreatedAt(Instant.now());
        tag.setUpdatedAt(Instant.now());
        tagRepository.save(tag);
        return tag;
    }

    public Tag updateTag(TagUpdateInt tagDTO) {
        Tag tag = tagRepository.getReferenceById(tagDTO.getTagId());

        if (tagDTO.getName() != null) {
            tag.setName(tagDTO.getName());
        }
        if (tagDTO.getDescription() != null) {
            tag.setDescription(tagDTO.getDescription());
        }
        tag.setUpdatedAt(Instant.now());
        tagRepository.save(tag);
        return tag;
    }

    public List<Tag> getAllUserTags(UUID userId) {
        return tagRepository.findByUser(userRepository.getReferenceById(userId));
    }

    public void deleteTag(UUID tag) {
        tagRepository.delete(tagRepository.getReferenceById(tag));
    }
}
