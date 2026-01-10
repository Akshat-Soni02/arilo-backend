package com.project_x.project_x_backend.controller;

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
@RequestMapping("/api/tags")
public class TagController {
    // delete tag

    @Autowired
    private TagService tagService;

    @Autowired
    private AuthService authService;

    // mapping to let user create tag
    @PostMapping("")
    public ResponseEntity<TagCreateResponse> createTag(@RequestHeader("Authorization") String authorization,
            @RequestBody TagCreateRequest tagCreateRequest) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Tag tag = tagService.createTag(userId, tagCreateRequest);
        return ResponseEntity.ok(new TagCreateResponse(tag.getId(), tag.getName(), tag.getDescription(),
                tag.getCreatedBy(), tag.getCreatedAt()));
    }

    // mapping to let user update tag
    @PutMapping("/{id}")
    public ResponseEntity<TagCreateResponse> updateTag(@RequestHeader("Authorization") String authorization,
            @PathVariable UUID id, @RequestBody TagUpdateRequest tagUpdateRequest) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Tag tag = tagService.updateTag(userId, id, tagUpdateRequest);
        return ResponseEntity.ok(new TagCreateResponse(tag.getId(), tag.getName(), tag.getDescription(),
                tag.getCreatedBy(), tag.getCreatedAt()));
    }

    @GetMapping("")
    public ResponseEntity<List<TagCreateResponse>> getAllTags(@RequestHeader("Authorization") String authorization) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Tag> tags = tagService.getAllTags(userId);
        List<TagCreateResponse> tagCreateResponses = tags.stream().map(tag -> new TagCreateResponse(tag.getId(),
                tag.getName(), tag.getDescription(), tag.getCreatedBy(), tag.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tagCreateResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        UUID userId = authService.extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }

}
