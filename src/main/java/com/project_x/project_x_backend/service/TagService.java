package com.project_x.project_x_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project_x.project_x_backend.dto.TagDTO.TagCreateInt;
import com.project_x.project_x_backend.dto.TagDTO.TagCreateRequest;
import com.project_x.project_x_backend.dto.TagDTO.TagUpdateInt;
import com.project_x.project_x_backend.dto.TagDTO.TagUpdateRequest;
import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.dao.TagDAO;
import com.project_x.project_x_backend.enums.TagSource;

@Service
public class TagService {

    @Autowired
    private TagDAO tagDAO;

    public Tag createTag(UUID userId, TagCreateRequest tagCreateRequest) {
        return tagDAO.createTag(new TagCreateInt(userId, tagCreateRequest.getName(), tagCreateRequest.getDescription(),
                TagSource.USER));
    }

    public Tag updateTag(UUID userId, UUID tagId, TagUpdateRequest tagUpdateRequest) {
        return tagDAO.updateTag(
                new TagUpdateInt(userId, tagId, tagUpdateRequest.getName(), tagUpdateRequest.getDescription()));
    }

    public List<Tag> getAllTags(UUID userId) {
        return tagDAO.getAllUserTags(userId);
    }

    public void deleteTag(UUID tagId) {
        tagDAO.deleteTag(tagId);
    }
}
