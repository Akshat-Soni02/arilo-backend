package com.project_x.project_x_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project_x.project_x_backend.dto.TagDTO.TagCreateRequest;
import com.project_x.project_x_backend.dto.TagDTO.TagCreateResponse;
import com.project_x.project_x_backend.dto.TagDTO.TagUpdateRequest;
import com.project_x.project_x_backend.entity.Tag;
import com.project_x.project_x_backend.service.AuthService;
import com.project_x.project_x_backend.service.TagService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/tags")
@Slf4j
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private AuthService authService;

    // mapping to let user create tag
    @PostMapping
    public ResponseEntity<TagCreateResponse> createTag(@RequestHeader("Authorization") String authorization,
            @RequestBody TagCreateRequest tagCreateRequest) {
        log.info("Received request to create tag: {}", tagCreateRequest.getName());
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized tag creation attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Tag tag = tagService.createTag(userId, tagCreateRequest);
            log.info("Successfully created tag {} for user {}", tag.getId(), userId);
            return ResponseEntity.ok(new TagCreateResponse(tag.getId(), tag.getName(), tag.getDescription(),
                    tag.getCreatedBy(), tag.getCreatedAt()));
        } catch (Exception e) {
            log.error("Error creating tag: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // mapping to let user update tag
    @PutMapping("/{id}")
    public ResponseEntity<TagCreateResponse> updateTag(@RequestHeader("Authorization") String authorization,
            @PathVariable UUID id, @RequestBody TagUpdateRequest tagUpdateRequest) {
        log.info("Received request to update tag {}", id);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized tag update attempt for tag {}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Tag tag = tagService.updateTag(userId, id, tagUpdateRequest);
            return ResponseEntity.ok(new TagCreateResponse(tag.getId(), tag.getName(), tag.getDescription(),
                    tag.getCreatedBy(), tag.getCreatedAt()));
        } catch (Exception e) {
            log.error("Error updating tag {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TagCreateResponse>> getAllTags(@RequestHeader("Authorization") String authorization) {
        log.info("Received request to get all tags");
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized request to get all tags");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Tag> tags = tagService.getAllTags(userId);
            List<TagCreateResponse> tagCreateResponses = tags.stream().map(tag -> new TagCreateResponse(tag.getId(),
                    tag.getName(), tag.getDescription(), tag.getCreatedBy(), tag.getCreatedAt()))
                    .collect(Collectors.toList());

            log.info("Returned {} tags for user {}", tags.size(), userId);
            return ResponseEntity.ok(tagCreateResponses);
        } catch (Exception e) {
            log.error("Error getting all tags: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        log.info("Received request to delete tag {}", id);
        try {
            UUID userId = authService.extractUserIdFromToken(authorization);
            if (userId == null) {
                log.warn("Unauthorized tag deletion attempt for tag {}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            tagService.deleteTag(id);
            log.info("Successfully deleted tag {} for user {}", id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting tag {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
