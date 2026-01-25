package com.project_x.project_x_backend.service;

import com.project_x.project_x_backend.dto.TagDTO.TagCreateInt;
import com.project_x.project_x_backend.dto.TagDTO.TagCreateRequest;
import com.project_x.project_x_backend.dto.TagDTO.TagUpdateInt;
import com.project_x.project_x_backend.dto.TagDTO.TagUpdateRequest;
import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.dao.TagDAO;
import com.project_x.project_x_backend.enums.TagSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TagService {

    @Autowired
    private TagDAO tagDAO;

    public Tag createTag(UUID userId, TagCreateRequest tagCreateRequest) {
        return tagDAO.createTag(new TagCreateInt(userId, tagCreateRequest.getName(), tagCreateRequest.getDescription(),
                TagSource.USER));
    }

    public Tag updateTag(UUID userId, UUID tagId, TagUpdateRequest tagUpdateRequest) {
        log.info("Updating tag ID: {} for user {}", tagId, userId);
        try {
            Tag tag = tagDAO.updateTag(
                    new TagUpdateInt(userId, tagId, tagUpdateRequest.getName(), tagUpdateRequest.getDescription()));
            log.info("Successfully updated tag ID: {} for user {}", tagId, userId);
            return tag;
        } catch (Exception e) {
            log.error("Failed to update tag {} for user {}: {}", tagId, userId, e.getMessage(), e);
            throw e;
        }
    }

    public List<Tag> getAllTags(UUID userId) {
        return tagDAO.getAllUserTags(userId);
    }

    public void deleteTag(UUID tagId) {
        log.info("Deleting tag ID: {}", tagId);
        try {
            tagDAO.deleteTag(tagId);
            log.info("Successfully deleted tag ID: {}", tagId);
        } catch (Exception e) {
            log.error("Failed to delete tag {}: {}", tagId, e.getMessage(), e);
            throw e;
        }
    }
}
