package com.project_x.project_x_backend.dao;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project_x.project_x_backend.dto.TagDTO.TagDTO;
import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.repository.TagRepository;
import com.project_x.project_x_backend.repository.UserRepository;

@Component
public class TagDAO {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    public void createTag(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setUser(userRepository.getReferenceById(tagDTO.getUserId()));
        tag.setName(tagDTO.getTag());
        tag.setTag(tagDTO.getTag());
        tag.setCreatedAt(Instant.now());
        tag.setUpdatedAt(Instant.now());
        tagRepository.save(tag);
    }
}
